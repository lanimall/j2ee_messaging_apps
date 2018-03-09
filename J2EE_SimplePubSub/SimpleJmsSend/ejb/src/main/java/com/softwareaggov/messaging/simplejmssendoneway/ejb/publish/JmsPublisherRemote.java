package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish;

import javax.jms.JMSException;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/15/16.
 */
public interface JmsPublisherRemote {
    //TODO: should change the method return to ProcessorOutput interface instead of a simple text...
    String sendTextMessage(final Object msgTextPayload, final Map<String, Object> msgHeaderProperties) throws JMSException;

    boolean isEnabled();
}