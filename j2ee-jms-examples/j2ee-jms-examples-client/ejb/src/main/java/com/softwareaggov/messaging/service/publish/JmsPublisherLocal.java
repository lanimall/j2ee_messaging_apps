package com.softwareaggov.messaging.service.publish;

import java.util.Map;

/**
 * Created by fabien.sanglier on 6/15/16.
 */
public interface JmsPublisherLocal {
    String sendTextMessage(final String msgTextPayload, final Map<String,String> msgHeaderProperties);
}