package com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe;

import com.softwareaggov.messaging.libs.jms.processor.ProcessorOutput;
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
            //post processing
            Object postProcessingPayload = null;
            Map<JMSHelper.JMSHeadersType, Object> postProcessingJMSHeaderProperties = null;
            Map<String, Object> postProcessingCustomProperties = null;

            try {
                if (log.isDebugEnabled()) {
                    Object preProcessingPayload = JMSHelper.getMessagePayload(msg);
                    String preProcessingJMSHeaderPropertiesStr = JMSHelper.getMessageJMSHeaderPropsAsString(msg, ",");
                    String preProcessingCustomPropertiesStr = JMSHelper.getMessagePropertiesAsString(msg, ",");

                    log.debug("Received message before any processing: {}, \nJMS Headers: {}, \nCustom Properties: {}",
                            ((null != preProcessingPayload) ? preProcessingPayload : "null"),
                            ((null != preProcessingJMSHeaderPropertiesStr) ? preProcessingJMSHeaderPropertiesStr : "null"),
                            ((null != preProcessingCustomPropertiesStr) ? preProcessingCustomPropertiesStr : "null"));
                }

                //processing the message
                if (null == messageProcessor)
                    throw new IllegalArgumentException("Message Processor is null...unexpected.");

                //process the message
                ProcessorOutput processorResult = messageProcessor.processMessage(msg);

                if (null != processorResult) {
                    postProcessingPayload = processorResult.getMessagePayload();
                    postProcessingJMSHeaderProperties = processorResult.getJMSHeaderProperties();
                    postProcessingCustomProperties = processorResult.getMessageProperties();
                }

                messageProcessingCounter.incrementAndGet(getBeanName() + "-processingSuccess");
            } catch (Exception e) {
                messageProcessingCounter.incrementAndGet(getBeanName() + "-processingErrors");
                throw new EJBException(e);
            }

            if (jmsMessageEnableReply && null != postProcessingJMSHeaderProperties) {
                try {
                    initJMSReply();

                    //if replyTo overrides default, use it first, and only use default if the message replyTo is null
                    //otherwise, force to using the default always
                    Destination replyTo = null;
                    if (jmsMessageReplyOverridesDefault) {
                        if (null != postProcessingJMSHeaderProperties.get(JMSHelper.JMSHeadersType.JMS_REPLYTO))
                            replyTo = (Destination) postProcessingJMSHeaderProperties.get(JMSHelper.JMSHeadersType.JMS_REPLYTO);

                        if (null == replyTo)
                            replyTo = jmsDefaultReplyTo;
                    } else {
                        replyTo = jmsDefaultReplyTo;
                    }

                    //if replyTo is set, reply
                    if (null != replyTo) {
                        //set the destination to the replyTo
                        postProcessingJMSHeaderProperties.put(JMSHelper.JMSHeadersType.JMS_DESTINATION, replyTo);

                        // Get correlationID from message if set.
                        // If not set, then get the MessageID from message, and set the reply correlationID with it
                        if (null == postProcessingJMSHeaderProperties.get(JMSHelper.JMSHeadersType.JMS_CORRELATIONID))
                            postProcessingJMSHeaderProperties.put(JMSHelper.JMSHeadersType.JMS_CORRELATIONID, postProcessingJMSHeaderProperties.get(JMSHelper.JMSHeadersType.JMS_MESSAGEID));

                        //send reply
                        jmsHelper.sendTextMessage(postProcessingPayload, postProcessingJMSHeaderProperties, postProcessingCustomProperties);
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