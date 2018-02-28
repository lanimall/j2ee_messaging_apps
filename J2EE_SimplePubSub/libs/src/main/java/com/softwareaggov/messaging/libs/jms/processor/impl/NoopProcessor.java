package com.softwareaggov.messaging.libs.jms.processor.impl;

import com.softwareaggov.messaging.libs.jms.processor.MessageProcessor;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.AbstractMap;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

public class NoopProcessor implements MessageProcessor {
    @Override
    public Map.Entry<String, Map<String, Object>> processMessage(Message msg) throws JMSException {
        String payloadResult = String.format("Mock message processing - doing nothing");
        return new AbstractMap.SimpleImmutableEntry<String, Map<String, Object>>(
                payloadResult, null
        );
    }
}