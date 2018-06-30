package com.softwareaggov.messaging.libs.jms.processor;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

public interface MessageProcessor {
    ProcessorOutput processMessage(Message message) throws JMSException;
}
