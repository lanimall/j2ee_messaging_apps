package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/28/16.
 */

@Stateless(mappedName = "JmsSendAndForgetBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(JmsPublisherLocal.class)
public class JmsSendAndForgetBean extends JmsPublisherBase implements JmsPublisherLocal {
    private static Logger log = LoggerFactory.getLogger(JmsSendAndForgetBean.class);

    @Resource(name = "jms/someManagedCF")
    private ConnectionFactory jmsConnectionFactory;

    @Resource(name = "jms/someManagedDestination")
    private Destination jmsDestination;

    @Override
    public ConnectionFactory getJmsConnectionFactory() {
        return jmsConnectionFactory;
    }

    @Override
    public Destination getJmsDestination() {
        return jmsDestination;
    }

    @Override
    protected String sendMessage(Destination destination, final String payload, final Map<String, String> headerProperties, Integer deliveryMode, Integer priority, String correlationID, Destination replyTo) throws JMSException {
        return jmsHelper.sendTextMessage(destination, payload, headerProperties, deliveryMode, priority, correlationID, replyTo);
    }
}
