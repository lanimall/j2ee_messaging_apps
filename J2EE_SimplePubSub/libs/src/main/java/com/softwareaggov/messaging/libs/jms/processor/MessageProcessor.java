package com.softwareaggov.messaging.libs.jms.processor;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

public interface MessageProcessor {
    Map.Entry<String, Map<String, Object>> processMessage(Message message) throws JMSException;
}
