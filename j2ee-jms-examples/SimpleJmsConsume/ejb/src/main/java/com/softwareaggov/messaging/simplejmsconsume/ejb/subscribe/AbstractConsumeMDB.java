package com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe;

import com.softwareaggov.messaging.libs.utils.JMSHelper;
import com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe.processor.MessageProcessorLocal;
import com.softwareaggov.messaging.simplejmsconsume.ejb.utils.CounterLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
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

public abstract class AbstractConsumeMDB implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602751473208935601L;

    private static Logger log = LoggerFactory.getLogger(AbstractConsumeMDB.class);

    @EJB
    private CounterLocal messageProcessingCounter;

    private MessageDrivenContext mdbContext;
    private transient JMSHelper jmsHelper;
    private transient Destination jmsDefaultReplyTo = null;

    @Resource(name = "jmsMessageEnableReply")
    private Boolean jmsMessageEnableReply = false;

    @Resource(name = "jmsReplyDestinationName")
    private String jmsReplyDestinationName = null;

    @Resource(name = "jmsReplyDestinationType")
    private String jmsReplyDestinationType = null;

    @Resource(name = "jmsMessageReplyOverridesDefault")
    private Boolean jmsMessageReplyOverridesDefault = true;

    @Resource(name = "jms/someManagedReplyCF")
    private ConnectionFactory jmsConnectionFactory = null;

    //get the implementation for the message processing
    protected abstract MessageProcessorLocal getMessageProcessor();

    public void ejbCreate() {
        log.info("ejbCreate()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-create");

        //create JMSHelper
        jmsDefaultReplyTo = null;
        if (null != jmsConnectionFactory) {
            jmsHelper = JMSHelper.createSender(jmsConnectionFactory);

            //JMS reply destination
            if (null != jmsReplyDestinationName && !"".equals(jmsReplyDestinationName) &&
                    null != jmsReplyDestinationType && !"".equals(jmsReplyDestinationType)) {
                try {
                    jmsDefaultReplyTo = jmsHelper.lookupDestination(jmsReplyDestinationName, jmsReplyDestinationType);
                } catch (Exception e) {
                    log.error("Could not lookup/create the replyTo Destination...Check that the lookup info is accurate!", e);
                }
            }
        }
    }

    public void ejbRemove() throws EJBException {
        log.info("ejbRemove()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-remove");
        jmsDefaultReplyTo = null;
        if (null != jmsHelper)
            jmsHelper.cleanup();
        jmsHelper = null;
    }

    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        mdbContext = ctx;
    }

    protected String getBeanName() {
        return this.getClass().getSimpleName();
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message msg) {
        if (log.isDebugEnabled())
            log.debug("onMessage() start");

        messageProcessingCounter.incrementAndGet(getBeanName() + "-consumed");

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

                messageProcessingCounter.incrementAndGet(getBeanName() + "-processed");

                if (jmsMessageEnableReply) {
                    //if replyTo overrides default, use it first, and only use default if the message replyTo is null
                    //otherwise, force to using the default always
                    Destination replyTo = null;
                    if (jmsMessageReplyOverridesDefault) {
                        replyTo = msg.getJMSReplyTo();
                        if (null == replyTo)
                            replyTo = jmsDefaultReplyTo;
                    } else {
                        replyTo = jmsDefaultReplyTo;
                    }

                    //if replyTo is set, reply
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
                        messageProcessingCounter.incrementAndGet(getBeanName() + "-replied");
                    }
                }
            } else {
                if (log.isWarnEnabled())
                    log.warn("Received Message from queue: null");
            }

        } catch (Exception e) {
            messageProcessingCounter.incrementAndGet(getBeanName() + "-errors");
            throw new EJBException(e);
        }
    }
}