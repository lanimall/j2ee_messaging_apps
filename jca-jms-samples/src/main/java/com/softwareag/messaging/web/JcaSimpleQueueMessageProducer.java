package com.softwareag.messaging.web;

import com.softwareag.messaging.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

/**
 * <p>
 * A servlet that sends several JMS messages to a JMS queue or a topic
 * as defined by the jmsDestination variable that is bound to a JCA admin object (hence using JCA construct)
 * </p>
 * <p>
 * The servlet is registered and mapped to /JcaQueueProxyMessageProducer using the {@linkplain javax.servlet.annotation.WebServlet
 * @author Fabien Sanglier
 * @HttpServlet}. </p>
 */
@WebServlet("/JcaSimpleQueueMessageProducer")
public class JcaSimpleQueueMessageProducer extends BaseMessageProducer {
    private static final long serialVersionUID = -8314702649252239L;
    private static Logger log = LoggerFactory.getLogger(JcaSimpleQueueMessageProducer.class);

    @Resource(name = "jms/someManagedQCF")
    private ConnectionFactory connectionFactory;

    @Resource(name = "jms/someManagedSimpleQueue")
    private Destination jmsDestination;

    @Override
    protected JMSHelper createMessageSender() throws ServletException {
        return JMSHelper.createSender(connectionFactory, jmsDestination);
    }
}
