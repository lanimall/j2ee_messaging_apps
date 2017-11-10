package com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe;

import com.softwareaggov.messaging.libs.utils.JMSHelper;
import com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe.processor.MessageProcessorLocal;
import com.softwareaggov.messaging.simplejmsconsume.ejb.utils.CounterLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;

/**
 * <p>
 * A simple Message Driven Bean that receives and processes messages through JMS constructs from defined JMS destinations
 * </p>
 *
 * @author Fabien Sanglier
 */

@TransactionManagement(value = TransactionManagementType.BEAN)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public abstract class AbstractConsumeMDB implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602751473208935601L;

    private static Logger log = LoggerFactory.getLogger(AbstractConsumeMDB.class);

    @EJB
    private CounterLocal messageProcessingCounter;

    private MessageDrivenContext mdbContext;
    private transient JMSHelper jmsHelper;

    //get the implementation for the message processing
    protected abstract MessageProcessorLocal getMessageProcessor();

    public abstract ConnectionFactory getReplyConnectionFactory();

    public void ejbRemove() throws EJBException {
        log.info("ejbRemove()");
        messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-remove");
    }

    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        mdbContext = ctx;
    }

    public void ejbCreate() {
        log.info("ejbCreate()");

        messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-create");

        //create the jmsHelper if connection factory is specified
        if (null != getReplyConnectionFactory())
            jmsHelper = JMSHelper.createSender(getReplyConnectionFactory());
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message msg) {
        if (log.isDebugEnabled())
            log.debug("onMessage() start");

        messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-consumed");

        try {
            if (null != msg) {
                //processing the message
                MessageProcessorLocal messageProcessor = getMessageProcessor();
                if (null == messageProcessor)
                    throw new EJBException("Message Processor is null...unexpected.");

                //process the message
                Map.Entry<String, Map<String, String>> result = messageProcessor.processMessage(msg);

                String postProcessingPayload = null;
                Map<String, String> postProcessingHeaderProperties = null;
                if (null != result) {
                    postProcessingPayload = result.getKey();
                    postProcessingHeaderProperties = result.getValue();
                }

                //would do something with the payload and header...in the meantime, print them if debug is enabled
                if (log.isDebugEnabled()) {
                    String postProcessingHeaders = null;
                    if (null != postProcessingHeaderProperties) {
                        for (Map.Entry<String, String> header : postProcessingHeaderProperties.entrySet()) {
                            postProcessingHeaders += String.format("[%s,%s]", header.getKey(), header.getValue());
                        }
                    }
                    log.debug("Payload: {}, Headers: {}",
                            ((null != postProcessingPayload) ? postProcessingPayload : "null"),
                            ((null != postProcessingHeaders) ? postProcessingHeaders : "null"));
                }

                messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-processed");

                //get the replyTo if it's set, and if it is, reply
                Destination replyTo = msg.getJMSReplyTo();
                if (null != replyTo) {
                    if (null == jmsHelper)
                        throw new IllegalStateException("ReplyTo is set in the received message, but no connectionFactory defined to send the reply...unexpected.");

                    int deliveryMode = msg.getJMSDeliveryMode();
                    int priority = msg.getJMSPriority();

                    // Get correlationID from message if set.
                    // If not set, then get the MessageID from message, and set the reply correlationID with it
                    String correlationId = msg.getJMSCorrelationID();
                    if (null == correlationId || "".equals(correlationId))
                        correlationId = msg.getJMSMessageID();

                    //send reply
                    jmsHelper.sendTextMessage(replyTo, postProcessingPayload, postProcessingHeaderProperties, deliveryMode, priority, correlationId, null);
                    messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-replied");
                }
            } else {
                if (log.isWarnEnabled())
                    log.warn("Received Message from queue: null");
            }

        } catch (Exception e) {
            messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-errors");
            throw new EJBException(e);
        }
    }
}