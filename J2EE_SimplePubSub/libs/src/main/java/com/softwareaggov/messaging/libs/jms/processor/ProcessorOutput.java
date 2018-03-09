package com.softwareaggov.messaging.libs.jms.processor;

import com.softwareaggov.messaging.libs.utils.JMSHelper;

import java.util.Map;

/**
 * Created by fabien.sanglier on 3/7/18.
 */
public interface ProcessorOutput {
    Object getMessagePayload();

    Map<String, Object> getMessageProperties();

    Map<JMSHelper.JMSHeadersType, Object> getJMSHeaderProperties();
}
