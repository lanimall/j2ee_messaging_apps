package com.softwareaggov.messaging.libs.jms.processor.impl;

import com.softwareaggov.messaging.libs.jms.processor.MessageProcessor;
import com.softwareaggov.messaging.libs.jms.processor.ProcessorOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJBException;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

public class MockSleepProcessor implements MessageProcessor {
    private static Logger log = LoggerFactory.getLogger(MockSleepProcessor.class);

    private final Long mockSleepTimeInMillis;
    private MessageCloneProcessor messageCloneProcessor;

    public MockSleepProcessor(Long mockSleepTimeInMillis, boolean cloneProperties) {
        this.mockSleepTimeInMillis = mockSleepTimeInMillis;
        if (cloneProperties)
            messageCloneProcessor = new MessageCloneProcessor();
    }

    @Override
    public ProcessorOutput processMessage(Message msg) throws JMSException {
        ProcessorOutput processingResult;

        String payload = null;
        if (null != mockSleepTimeInMillis && mockSleepTimeInMillis > 0) {
            payload = String.format("Sleeping %d ms to mock processing time...", mockSleepTimeInMillis);
            log.debug(payload);
            try {
                Thread.sleep(mockSleepTimeInMillis);
            } catch (InterruptedException e) {
                throw new EJBException(e);
            }
        }

        if (null != messageCloneProcessor) {
            processingResult = messageCloneProcessor.processMessage(msg);
        } else {
            processingResult = new ProcessorOutputImpl(
                    payload,
                    null,
                    null
            );
        }

        return processingResult;
    }
}