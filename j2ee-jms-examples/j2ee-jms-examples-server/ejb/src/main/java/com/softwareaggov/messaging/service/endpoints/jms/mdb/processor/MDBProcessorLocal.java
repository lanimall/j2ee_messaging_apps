package com.softwareaggov.messaging.service.endpoints.jms.mdb.processor;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

public interface MDBProcessorLocal {
    Map.Entry<String, Map<String, String>> processMessage(Message message) throws JMSException;
}
