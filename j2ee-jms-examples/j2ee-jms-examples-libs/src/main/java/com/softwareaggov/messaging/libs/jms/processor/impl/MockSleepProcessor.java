package com.softwareaggov.messaging.libs.jms.processor.impl;

import com.softwareaggov.messaging.libs.jms.processor.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJBException;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.AbstractMap;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

public class MockSleepProcessor implements MessageProcessor {
    private static Logger log = LoggerFactory.getLogger(MockSleepProcessor.class);

    private final Long mockSleepTimeInMillis;

    public MockSleepProcessor(Long mockSleepTimeInMillis) {
        this.mockSleepTimeInMillis = mockSleepTimeInMillis;
    }

    @Override
    public Map.Entry<String, Map<String, String>> processMessage(Message msg) throws JMSException {
        Map.Entry<String, Map<String, String>> processingResult = null;
        String payloadResult = null;

        if (null != mockSleepTimeInMillis && mockSleepTimeInMillis > 0) {
            log.debug("Sleeping " + mockSleepTimeInMillis + " to mock processing time...");
            try {
                Thread.sleep(mockSleepTimeInMillis);
            } catch (InterruptedException e) {
                throw new EJBException(e);
            }
        }

        payloadResult = String.format("Mock message processing - slept for %d ms", mockSleepTimeInMillis);

        //create the processingResult pair
        processingResult = new AbstractMap.SimpleImmutableEntry<String, Map<String, String>>(
                payloadResult, null
        );

        return processingResult;
    }
}