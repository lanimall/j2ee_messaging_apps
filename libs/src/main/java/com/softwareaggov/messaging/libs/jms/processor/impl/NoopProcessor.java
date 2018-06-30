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
 */

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