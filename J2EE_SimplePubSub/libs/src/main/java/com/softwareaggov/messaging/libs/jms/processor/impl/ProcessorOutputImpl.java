package com.softwareaggov.messaging.libs.jms.processor.impl;

import com.softwareaggov.messaging.libs.jms.processor.ProcessorOutput;
import com.softwareaggov.messaging.libs.utils.JMSHelper;

import java.util.Map;

/**
 * Created by fabien.sanglier on 3/7/18.
 */
public class ProcessorOutputImpl implements ProcessorOutput {
    private final Object postProcessingPayload;
    private final Map<JMSHelper.JMSHeadersType, Object> postProcessingJMSHeaderProperties;
    private final Map<String, Object> postProcessingCustomProperties;

    public ProcessorOutputImpl(Object postProcessingPayload, Map<JMSHelper.JMSHeadersType, Object> postProcessingJMSHeaderProperties, Map<String, Object> postProcessingCustomProperties) {
        this.postProcessingPayload = postProcessingPayload;
        this.postProcessingJMSHeaderProperties = postProcessingJMSHeaderProperties;
        this.postProcessingCustomProperties = postProcessingCustomProperties;
    }

    @Override
    public Object getMessagePayload() {
        return postProcessingPayload;
    }

    @Override
    public Map<String, Object> getMessageProperties() {
        return postProcessingCustomProperties;
    }

    @Override
    public Map<JMSHelper.JMSHeadersType, Object> getJMSHeaderProperties() {
        return postProcessingJMSHeaderProperties;
    }
}
