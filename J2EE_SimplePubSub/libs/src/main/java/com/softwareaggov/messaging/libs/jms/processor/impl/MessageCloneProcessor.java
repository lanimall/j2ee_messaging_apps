package com.softwareaggov.messaging.libs.jms.processor.impl;

import com.softwareaggov.messaging.libs.jms.processor.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

public class MessageCloneProcessor implements MessageProcessor {
    private static Logger log = LoggerFactory.getLogger(MessageCloneProcessor.class);

    private Object msgPayloadOverride;
    private Map<String, String> msgPropertiesOverride;
    private boolean overwriteAllProperties = false;

    public MessageCloneProcessor() {
    }

    public MessageCloneProcessor(Object msgPayloadOverride, Map<String, String> msgPropertiesOverride, boolean overwriteAllProperties) {
        this.msgPayloadOverride = msgPayloadOverride;
        this.msgPropertiesOverride = msgPropertiesOverride;
        this.overwriteAllProperties = overwriteAllProperties;
    }

    @Override
    public Map.Entry<String, Map<String, Object>> processMessage(Message msg) throws JMSException {
        Map.Entry<String, Map<String, Object>> processingResult = null;
        Object payloadResult = null;

        //copy the properties from the incoming message
        Map<String, Object> props = null;
        if (overwriteAllProperties) {
            if (null != msgPropertiesOverride && msgPropertiesOverride.size() > 0) {
                props = new HashMap();
                for (String propName : msgPropertiesOverride.keySet()) {
                    props.put(propName, msgPropertiesOverride.get(propName));
                }
            }
        } else {
            Enumeration txtMsgPropertiesEnum = msg.getPropertyNames();
            while (txtMsgPropertiesEnum.hasMoreElements()) {
                String propName = (String) txtMsgPropertiesEnum.nextElement();
                props.put(propName, msg.getObjectProperty(propName));
            }
        }

        if (msg instanceof TextMessage) {
            if (null != msgPayloadOverride) {
                if (msgPayloadOverride instanceof String)
                    payloadResult = msgPayloadOverride;
                else
                    throw new IllegalArgumentException("Cannot apply non-text msgPayloadOverride to a TextMessage");
            } else {
                payloadResult = ((TextMessage) msg).getText();
            }
            processingResult = new AbstractMap.SimpleImmutableEntry<String, Map<String, Object>>(
                    (String) payloadResult, props
            );
        } else if (msg instanceof MapMessage) {
            throw new UnsupportedOperationException("Not implemented yet");
        } else if (msg instanceof BytesMessage) {
            throw new UnsupportedOperationException("Not implemented yet");
        } else if (msg instanceof ObjectMessage) {
            throw new UnsupportedOperationException("Not implemented yet");
        } else if (msg instanceof StreamMessage) {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        return processingResult;
    }
}