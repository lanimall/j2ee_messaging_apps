package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import java.util.Map;

/*
 * Simple JMS Publisher bean relying on the connection factory to create the JMS connection on each call
 * Created by fabien.sanglier on 6/15/16.
 */
public abstract class JmsPublisherSyncWaitBaseBean extends JmsPublisherOneWayBaseBean implements JmsPublisherLocal {
    private static Logger log = LoggerFactory.getLogger(JmsPublisherSyncWaitBaseBean.class);

    @Resource(name = "jmsResponseWaitMillis")
    private Long jmsResponseWaitMillis = null;

    @Override
    protected String sendMessage(Destination destination, String payload, Map<String, String> headerProperties, Integer deliveryMode, Integer priority, String correlationID, Destination replyTo) throws JMSException {
        return jmsHelper.sendTextMessageAndWait(destination, payload, headerProperties, deliveryMode, priority, replyTo, jmsResponseWaitMillis);
    }
}
