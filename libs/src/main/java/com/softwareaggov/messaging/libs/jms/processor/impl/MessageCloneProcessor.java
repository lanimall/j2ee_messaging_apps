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
import com.softwareaggov.messaging.libs.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
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
    public ProcessorOutput processMessage(Message msg) throws JMSException {
        ProcessorOutput processingResult = null;
        Object msgPayload;
        Map<JMSHelper.JMSHeadersType, Object> msgJMSHeaderProperties;
        Map<String, Object> msgCustomProperties;

        //get the JMS properties from the incoming message
        msgJMSHeaderProperties = JMSHelper.getMessageJMSHeaderPropsAsMap(msg);

        //custom properties from the incoming message
        //if the map msgPropertiesOverride is set, override the message props
        if (!overwritePropertiesEnabled || overwritePropertiesEnabled && mergePropertyEnabled) {
            msgCustomProperties = JMSHelper.getMessageProperties(msg);
        } else {
            msgCustomProperties = new HashMap();
        }

        //create a property merge between the msg properties and the properties passed in the msgPropertiesOverride (which should override the message properties)
        if (overwritePropertiesEnabled) {
            if (null != msgPropertiesOverride && msgPropertiesOverride.size() > 0)
                msgCustomProperties.putAll(msgPropertiesOverride);
        }

        //copy the payload from the incoming message
        msgPayload = JMSHelper.getMessagePayload(msg);
        if (overwritePayloadEnabled) {
            msgPayload = msgPayloadOverride;
        }

        // Packaging the payload + properties into processorOutput object
        processingResult = new ProcessorOutputImpl(
                msgPayload,
                msgJMSHeaderProperties,
                msgCustomProperties
        );

        return processingResult;
    }
}