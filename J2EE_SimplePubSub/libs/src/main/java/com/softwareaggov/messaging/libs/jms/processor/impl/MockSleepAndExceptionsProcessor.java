package com.softwareaggov.messaging.libs.jms.processor.impl;

import com.softwareaggov.messaging.libs.jms.processor.MessageProcessor;
import com.softwareaggov.messaging.libs.utils.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

public class MockSleepAndExceptionsProcessor extends MockSleepProcessor implements MessageProcessor {
    private static Logger log = LoggerFactory.getLogger(MockSleepAndExceptionsProcessor.class);

    private final Counter messageProcessingCounter;
    private final Integer mockExceptionsCountInterval;

    public MockSleepAndExceptionsProcessor(Long mockSleepTimeInMillis, Integer mockExceptionsCountInterval, Counter messageProcessingCounter) {
        super(mockSleepTimeInMillis);
        this.messageProcessingCounter = messageProcessingCounter;
        this.mockExceptionsCountInterval = mockExceptionsCountInterval;
    }

    @Override
    public Map.Entry<String, Map<String, Object>> processMessage(Message msg) throws JMSException {
        Map.Entry<String, Map<String, Object>> processingResult = super.processMessage(msg);

        if (null == messageProcessingCounter)
            throw new JMSException("Cannot do anything without a counter");

        long newCount = messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-exceptioncounter");

        if (null != mockExceptionsCountInterval && mockExceptionsCountInterval > 0) {
            if (newCount % mockExceptionsCountInterval == 0) {
                throw new JMSException("This is a mocked exception to mock failed processing");
            }
        }

        //don't change the output from super...if there's an exception, output won't matter
        return processingResult;
    }
}