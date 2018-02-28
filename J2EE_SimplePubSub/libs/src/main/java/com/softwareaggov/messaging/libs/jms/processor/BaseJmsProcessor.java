package com.softwareaggov.messaging.libs.jms.processor;

import com.softwareaggov.messaging.libs.utils.Counter;
import com.softwareaggov.messaging.libs.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.AbstractMap;
import java.util.Map;

/**
 * <p>
 * A simple Message Driven Bean that receives and processes messages through JMS constructs from defined JMS destinations
 * </p>
 *
 * @author Fabien Sanglier
 */

public class BaseJmsProcessor implements MessageProcessor {
    private static final long serialVersionUID = -4602751473208935601L;

    private static Logger log = LoggerFactory.getLogger(BaseJmsProcessor.class);

    //get the implementation for the message processing
    protected MessageProcessor messageProcessor;
    protected Counter messageCounter;
    private transient JMSHelper jmsHelper;

    public BaseJmsProcessor(MessageProcessor messageProcessor, Counter messageCounter, ConnectionFactory replyConnectionFactory) {
        this.messageProcessor = messageProcessor;
        this.messageCounter = messageCounter;

        //create the jmsHelper if connection factory is specified
        if (null != replyConnectionFactory)
            jmsHelper = JMSHelper.createSender(replyConnectionFactory);
    }

    private void incrementCounter(final String counterNameSuffix) {
        if (null != messageCounter)
            messageCounter.incrementAndGet(this.getClass().getSimpleName() + "-" + counterNameSuffix);
    }

    @Override
    public Map.Entry<String, Map<String, Object>> processMessage(Message msg) throws JMSException {
        //post processing
        String postProcessingPayload = null;
        Map<String, Object> postProcessingHeaderProperties = null;

        if (log.isDebugEnabled())
            log.debug("processMessage() start");

        incrementCounter("consumed");

        try {
            if (null != msg) {
                //processing the message
                Map.Entry<String, Map<String, Object>> processorResult = null;
                if (null != messageProcessor) {
                    processorResult = messageProcessor.processMessage(msg);
                    incrementCounter("processed");
                }

                if (null != processorResult) {
                    postProcessingPayload = processorResult.getKey();
                    postProcessingHeaderProperties = processorResult.getValue();
                }

                //would do something with the payload and header...in the meantime, print them if debug is enabled
                if (log.isDebugEnabled()) {
                    String postProcessingHeaders = null;
                    if (null != postProcessingHeaderProperties) {
                        for (Map.Entry<String, Object> header : postProcessingHeaderProperties.entrySet()) {
                            postProcessingHeaders += String.format("[%s,%s]", header.getKey(), (null != header.getValue()) ? header.getValue().toString() : "null");
                        }
                    }
                    log.debug("Payload: {}, Headers: {}",
                            ((null != postProcessingPayload) ? postProcessingPayload : "null"),
                            ((null != postProcessingHeaders) ? postProcessingHeaders : "null"));
                }

                //get the replyTo if it's set, and if it is, reply
                Destination replyTo = msg.getJMSReplyTo();
                if (null != replyTo && null != jmsHelper) {
                    int deliveryMode = msg.getJMSDeliveryMode();
                    int priority = msg.getJMSPriority();

                    //get MessageID from message, and set the reply correlationID with it
                    String correlationId = msg.getJMSMessageID();

                    //send reply
                    jmsHelper.sendTextMessage(replyTo, postProcessingPayload, postProcessingHeaderProperties, deliveryMode, priority, correlationId, null);
                    incrementCounter("replied");
                }
            } else {
                if (log.isWarnEnabled())
                    log.warn("Received Message from queue: null");
            }
        } catch (JMSException e) {
            incrementCounter("errors");
            throw e;
        }

        return new AbstractMap.SimpleImmutableEntry<String, Map<String, Object>>(
                postProcessingPayload, postProcessingHeaderProperties
        );
    }
}