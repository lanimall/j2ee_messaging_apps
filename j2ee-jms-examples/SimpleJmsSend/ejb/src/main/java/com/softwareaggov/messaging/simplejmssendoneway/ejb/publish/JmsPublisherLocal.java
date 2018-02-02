package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish;

import javax.jms.JMSException;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/15/16.
 */
public interface JmsPublisherLocal {
    String sendTextMessage(final String msgTextPayload, final Map<String, String> msgHeaderProperties) throws JMSException;
}