package com.softwareaggov.messaging.libs.jms.processor.impl;

import com.softwareaggov.messaging.libs.jms.processor.MessageProcessor;
import com.softwareaggov.messaging.libs.jms.processor.ProcessorOutput;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

public class NoopProcessor implements MessageProcessor {
    @Override
    public ProcessorOutput processMessage(Message msg) throws JMSException {
        String payload = String.format("Mock message processing - doing nothing");

        // Packaging the payload + properties into processorOutput object
        ProcessorOutput processingResult = new ProcessorOutputImpl(
                payload,
                null,
                null
        );

        return processingResult;
    }
}