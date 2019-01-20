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

package com.softwareaggov.messaging.libs.jms.processor;

import com.softwareaggov.messaging.libs.jms.processor.impl.ProcessorOutputImpl;
import com.softwareaggov.messaging.libs.utils.Counter;
import com.softwareaggov.messaging.libs.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Map;

/**
 * <p>
 * A simple Message Driven Bean that receives and processes messages through JMS constructs from defined JMS destinations
 * </p>
 *
 * @author Fabien Sanglier
 */

// FAS: @TODO Not used?? remove if useless...
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
    public ProcessorOutput processMessage(Message msg) throws JMSException {
        //post processing
        Object postProcessingPayload = null;
        Map<JMSHelper.JMSHeadersType, Object> postProcessingJMSHeaderProperties = null;
        Map<String, Object> postProcessingCustomProperties = null;

        if (log.isDebugEnabled())
            log.debug("processMessage() start");

        incrementCounter("consumed");

        try {
            if (null != msg) {
                //processing the message
                ProcessorOutput processorResult = null;
                if (null != messageProcessor) {
                    processorResult = messageProcessor.processMessage(msg);
                    incrementCounter("processed");
                }

                if (null != processorResult) {
                    postProcessingPayload = processorResult.getMessagePayload();
                    postProcessingJMSHeaderProperties = processorResult.getJMSHeaderProperties();
                    postProcessingCustomProperties = processorResult.getMessageProperties();
                }

                //would do something with the payload and header...in the meantime, print them if debug is enabled
                if (log.isDebugEnabled()) {
                    String postProcessingCustomHeaders = null;
                    String postProcessingJMSHeaders = null;

                    if (null != postProcessingJMSHeaderProperties) {
                        for (Map.Entry<JMSHelper.JMSHeadersType, Object> header : postProcessingJMSHeaderProperties.entrySet()) {
                            postProcessingJMSHeaders += String.format("[%s,%s]", header.getKey(), (null != header.getValue()) ? header.getValue().toString() : "null");
                        }
                    }

                    if (null != postProcessingCustomProperties) {
                        for (Map.Entry<String, Object> header : postProcessingCustomProperties.entrySet()) {
                            postProcessingCustomHeaders += String.format("[%s,%s]", header.getKey(), (null != header.getValue()) ? header.getValue().toString() : "null");
                        }
                    }

                    log.debug("Payload: {}, \nJMS Headers: {}, \nCustom Properties: {}",
                            ((null != postProcessingPayload) ? postProcessingPayload : "null"),
                            ((null != postProcessingJMSHeaders) ? postProcessingJMSHeaders : "null"),
                            ((null != postProcessingCustomHeaders) ? postProcessingCustomHeaders : "null"));
                }

                //get the replyTo if it's set, and if it is, reply
                Object replyTo = postProcessingJMSHeaderProperties.get(JMSHelper.JMSHeadersType.JMS_REPLYTO);
                if (null != replyTo && null != jmsHelper) {
                    // Get correlationID from message if set.
                    // If not set, then get the MessageID from message, and set the reply correlationID with it
                    if (null == postProcessingJMSHeaderProperties.get(JMSHelper.JMSHeadersType.JMS_CORRELATIONID))
                        postProcessingJMSHeaderProperties.put(JMSHelper.JMSHeadersType.JMS_CORRELATIONID, postProcessingJMSHeaderProperties.get(JMSHelper.JMSHeadersType.JMS_MESSAGEID));

                    //send reply
                    jmsHelper.sendAndForgetTextMessage(postProcessingPayload, postProcessingJMSHeaderProperties, postProcessingCustomProperties);
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

        return new ProcessorOutputImpl(
                postProcessingPayload,
                postProcessingJMSHeaderProperties,
                postProcessingCustomProperties
        );
    }
}