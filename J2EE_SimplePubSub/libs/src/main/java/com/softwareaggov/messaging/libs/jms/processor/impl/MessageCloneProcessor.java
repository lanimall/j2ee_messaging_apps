package com.softwareaggov.messaging.libs.jms.processor.impl;

import com.softwareaggov.messaging.libs.jms.processor.MessageProcessor;
import com.softwareaggov.messaging.libs.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

public class MessageCloneProcessor implements MessageProcessor {
    private static Logger log = LoggerFactory.getLogger(MessageCloneProcessor.class);

    private static boolean DEFAULT_OVERWRITE_PAYLOAD_ENABLED = false;
    private static boolean DEFAULT_OVERWRITE_PROPERTIES_ENABLED = false;
    private static boolean DEFAULT_MERGE_PROPERTIES_ENABLED = true;

    private boolean overwritePayloadEnabled = DEFAULT_OVERWRITE_PAYLOAD_ENABLED;
    private Object msgPayloadOverride = null;

    private boolean overwritePropertiesEnabled = DEFAULT_OVERWRITE_PROPERTIES_ENABLED;
    private Map<String, Object> msgPropertiesOverride = null;
    private boolean mergePropertyEnabled = DEFAULT_MERGE_PROPERTIES_ENABLED;

    public MessageCloneProcessor() {
    }

    public MessageCloneProcessor(Boolean overwritePayloadEnabled, Object msgPayloadOverride, Boolean overwritePropertiesEnabled, Map<String, Object> msgPropertiesOverride, Boolean mergePropertyEnabled) {
        this.overwritePayloadEnabled = (null != overwritePayloadEnabled) ? overwritePayloadEnabled.booleanValue() : DEFAULT_OVERWRITE_PAYLOAD_ENABLED;
        this.msgPayloadOverride = msgPayloadOverride;
        this.overwritePropertiesEnabled = (null != overwritePropertiesEnabled) ? overwritePropertiesEnabled.booleanValue() : DEFAULT_OVERWRITE_PROPERTIES_ENABLED;
        this.msgPropertiesOverride = msgPropertiesOverride;
        this.mergePropertyEnabled = (null != mergePropertyEnabled) ? mergePropertyEnabled.booleanValue() : DEFAULT_MERGE_PROPERTIES_ENABLED;
    }

    @Override
    public Map.Entry<String, Map<String, Object>> processMessage(Message msg) throws JMSException {
        Map.Entry<String, Map<String, Object>> processingResult = null;
        Object payloadResult = null;

        //copy the properties from the incoming message
        Map<String, Object> props = new HashMap();

        //if the map msgPropertiesOverride is set, override the message props
        if (!overwritePropertiesEnabled || overwritePropertiesEnabled && mergePropertyEnabled) {
            props = JMSHelper.getMessageProperties(msg);
        } else {
            props = new HashMap();
        }

        //create a property merge between the msg properties and the properties passed in the msgPropertiesOverride (which should override the message properties)
        if (overwritePropertiesEnabled) {
            if (null != msgPropertiesOverride && msgPropertiesOverride.size() > 0)
                props.putAll(msgPropertiesOverride);
        }

        //copy the payload from the incoming message
        payloadResult = JMSHelper.getMessagePayload(msg);
        if (overwritePayloadEnabled) {
            payloadResult = msgPayloadOverride;
        }

        // Packaging the payload + properties into an immutablke map
        // For now, only Text message payload is supported...
        processingResult = new AbstractMap.SimpleImmutableEntry<String, Map<String, Object>>(
                (String) payloadResult, props
        );

        return processingResult;
    }
}