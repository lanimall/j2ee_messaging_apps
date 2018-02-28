package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/28/16.
 */

@Stateless(name = "JmsSendAndWaitService")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(JmsPublisherLocal.class)
@Remote(JmsPublisherRemote.class)
public class JmsSendAndWaitBean extends JmsPublisherBase implements JmsPublisherLocal, JmsPublisherRemote {
    private static Logger log = LoggerFactory.getLogger(JmsSendAndWaitBean.class);

    @Resource(name = "jms/someManagedCF")
    private ConnectionFactory jmsConnectionFactory;

    @Resource(name = "jms/someManagedDestination")
    private Destination jmsDestination;

    @Resource(name = "jmsResponseWaitMillis")
    private Long jmsResponseWaitMillis = null;

    @Override
    public ConnectionFactory getJmsConnectionFactory() {
        return jmsConnectionFactory;
    }

    @Override
    public Destination getJmsDestination() {
        return jmsDestination;
    }

    @Override
    protected String sendMessage(Destination destination, String payload, Map<String, Object> headerProperties, Integer deliveryMode, Integer priority, String correlationID, Destination replyTo) throws JMSException {
        return jmsHelper.sendTextMessageAndWait(destination, payload, headerProperties, deliveryMode, priority, replyTo, jmsResponseWaitMillis);
    }
}
