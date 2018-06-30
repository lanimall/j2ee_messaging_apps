/*
 * Copyright © 2016 - 2018 Software AG, Darmstadt, Germany and/or its licensors
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */com.softwareaggov.messaging.libs.jms.processor.impl;

import com.softwareaggov.messaging.libs.jms.processor.MessageProcessor;
import com.softwareaggov.messaging.libs.jms.processor.ProcessorOutput;
import com.softwareaggov.messaging.libs.utils.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

public class MockSleepAndExceptionsProcessor extends MockSleepProcessor implements MessageProcessor {
    private static Logger log = LoggerFactory.getLogger(MockSleepAndExceptionsProcessor.class);

    private final Counter messageProcessingCounter;
    private final Integer mockExceptionsCountInterval;

    public MockSleepAndExceptionsProcessor(Long mockSleepTimeInMillis, Integer mockExceptionsCountInterval, Counter messageProcessingCounter, boolean cloneProperties) {
        super(mockSleepTimeInMillis, cloneProperties);
        this.messageProcessingCounter = messageProcessingCounter;
        this.mockExceptionsCountInterval = mockExceptionsCountInterval;
    }

    @Override
    public ProcessorOutput processMessage(Message msg) throws JMSException {
        ProcessorOutput processingResult = super.processMessage(msg);

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