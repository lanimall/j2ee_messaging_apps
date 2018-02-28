package com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe;

import com.softwareaggov.messaging.libs.utils.JMSHelper;
import com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe.processor.MessageProcessorLocal;
import com.softwareaggov.messaging.simplejmsconsume.ejb.utils.CounterLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.*;
import java.util.Map;

/**
 * <p>
 * A simple Message Driven Bean that receives and processes messages through JMS constructs from defined JMS destinations
 * </p>
 *
 * @author Fabien Sanglier
 */

@MessageDriven(name = "MessageConsumerService")
@TransactionManagement(value = TransactionManagementType.BEAN)
public class MessageConsumerServiceBean implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602751473208935601L;

    private static Logger log = LoggerFactory.getLogger(MessageConsumerServiceBean.class);

    @EJB
    private CounterLocal messageProcessingCounter;

    //Implementation for the message processing
    @EJB(name = "ejb/messageProcessor")
    private MessageProcessorLocal messageProcessor;

    private MessageDrivenContext mdbContext;
    private transient JMSHelper jmsHelper;
    private transient Destination jmsDefaultReplyTo = null;
    private volatile boolean init = false;

    @Resource(name = "jmsMessageEnableReply")
    private Boolean jmsMessageEnableReply = false;

    @Resource(name = "jmsReplyDefaultDestinationName")
    private String jmsReplyDefaultDestinationName = null;

    @Resource(name = "jmsReplyDefaultDestinationType")
    private String jmsReplyDefaultDestinationType = null;

    @Resource(name = "jmsMessageReplyOverridesDefault")
    private Boolean jmsMessageReplyOverridesDefault = true;

    @Resource(name = "jms/someManagedReplyCF")
    private ConnectionFactory jmsConnectionFactory = null;

    @PostConstruct
    public void ejbCreate() {
        log.info("ejbCreate()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-create");
    }

    @PreDestroy
    public void ejbRemove() throws EJBException {
        log.info("ejbRemove()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-remove");
        jmsDefaultReplyTo = null;
        init = false;
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

    // Initializing resource outside the EJB creation to make sure these lookups get retried until they work
    // (eg. if UM is not available yet when EJB is created)
    private void initJMSReply() throws JMSException {
        if (!init) {
            synchronized (this.getClass()) {
                if (!init) {
                    try {
                        this.jmsHelper = JMSHelper.createSender(jmsConnectionFactory);

                        //JMS reply destination
                        if (null != jmsReplyDefaultDestinationName && !"".equals(jmsReplyDefaultDestinationName) &&
                                null != jmsReplyDefaultDestinationType && !"".equals(jmsReplyDefaultDestinationType)) {
                            jmsDefaultReplyTo = jmsHelper.lookupDestination(jmsReplyDefaultDestinationName, jmsReplyDefaultDestinationType);
                        }

                        init = true;
                        messageProcessingCounter.incrementAndGet(getBeanName() + "-initReply");
                    } catch (JMSException e) {
                        messageProcessingCounter.incrementAndGet(getBeanName() + "-initReplyErrors");
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message msg) {
        if (log.isDebugEnabled())
            log.debug("onMessage() start");

        messageProcessingCounter.incrementAndGet(getBeanName() + "-messageConsumed");

        if (null != msg) {
            String postProcessingPayload = null;
            Map<String, Object> postProcessingHeaderProperties = null;

            try {
                if (log.isDebugEnabled()) {
                    Map<String, Object> preProcessingHeaders = JMSHelper.getMessageProperties(msg);
                    Object preProcessingPayload = JMSHelper.getMessagePayload(msg);

                    //transform the property map into a string
                    String preProcessingHeadersStr = "";
                    if (null != preProcessingHeaders) {
                        for (Map.Entry<String, Object> header : preProcessingHeaders.entrySet()) {
                            preProcessingHeadersStr += String.format("[%s,%s]", header.getKey(), (null != header.getValue()) ? header.getValue().toString() : "null");
                        }
                    }

                    log.debug("Pre-Processing Message - \nPayload: {}, \nHeaders: {}",
                            ((null != preProcessingPayload) ? preProcessingPayload.toString() : "null"),
                            ((null != preProcessingHeadersStr) ? preProcessingHeadersStr : "null"));
                }

                //processing the message
                if (null == messageProcessor)
                    throw new IllegalArgumentException("Message Processor is null...unexpected.");

                //process the message
                Map.Entry<String, Map<String, Object>> result = messageProcessor.processMessage(msg);

                if (null != result) {
                    postProcessingPayload = result.getKey();
                    postProcessingHeaderProperties = result.getValue();
                }

                //would do something with the payload and header...in the meantime, print them if debug is enabled
                if (log.isDebugEnabled()) {
                    //transform the property map into a string
                    String postProcessingHeadersStr = "";
                    if (null != postProcessingHeaderProperties) {
                        for (Map.Entry<String, Object> header : postProcessingHeaderProperties.entrySet()) {
                            postProcessingHeadersStr += String.format("[%s,%s]", header.getKey(), (null != header.getValue()) ? header.getValue().toString() : "null");
                        }
                    }
                    log.debug("Post-Processing Ouput - \nPayload: {}, \nHeaders: {}",
                            ((null != postProcessingPayload) ? postProcessingPayload.toString() : "null"),
                            ((null != postProcessingHeadersStr) ? postProcessingHeadersStr : "null"));
                }

                messageProcessingCounter.incrementAndGet(getBeanName() + "-processingSuccess");
            } catch (Exception e) {
                messageProcessingCounter.incrementAndGet(getBeanName() + "-processingErrors");
                throw new EJBException(e);
            }

            if (jmsMessageEnableReply) {
                try {
                    initJMSReply();

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
                        int deliveryMode = msg.getJMSDeliveryMode();
                        int priority = msg.getJMSPriority();

                        // Get correlationID from message if set.
                        // If not set, then get the MessageID from message, and set the reply correlationID with it
                        String correlationId = msg.getJMSCorrelationID();
                        if (null == correlationId || "".equals(correlationId))
                            correlationId = msg.getJMSMessageID();

                        //send reply
                        jmsHelper.sendTextMessage(replyTo, postProcessingPayload, postProcessingHeaderProperties, deliveryMode, priority, correlationId, null);
                        messageProcessingCounter.incrementAndGet(getBeanName() + "-replySuccess");
                    } else {
                        messageProcessingCounter.incrementAndGet(getBeanName() + "-replyNullDestination");
                    }
                } catch (Exception e) {
                    messageProcessingCounter.incrementAndGet(getBeanName() + "-replyErrors");
                    throw new EJBException(e);
                }
            }
        } else {
            messageProcessingCounter.incrementAndGet(getBeanName() + "-messageConsumedNull");
            if (log.isWarnEnabled())
                log.warn("Received Message from queue: null");
        }

    }
}