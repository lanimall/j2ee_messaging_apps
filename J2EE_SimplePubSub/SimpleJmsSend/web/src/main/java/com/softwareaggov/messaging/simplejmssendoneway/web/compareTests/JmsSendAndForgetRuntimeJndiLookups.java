package com.softwareaggov.messaging.simplejmssendoneway.web.compareTests;

import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsPublisherLocal;
import com.softwareaggov.messaging.simplejmssendoneway.web.BaseMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;

/**
 * <p>
 * A servlet that sends several JMS messages to a JMS queue or a topic
 * as defined by the jmsDestination variable that is bound to a JCA admin object (hence using JCA construct)
 * </p>
 * <p/>
 * The servlet is registered and mapped to /JcaQueueProxyMessageProducer using the {@linkplain javax.servlet.annotation.WebServlet
 *
 * @author Fabien Sanglier
 * @HttpServlet}. </p>
 */
@WebServlet("/JmsSendAndForgetRuntimeJndiLookups")
public class JmsSendAndForgetRuntimeJndiLookups extends BaseMessageProducer {
    private static final long serialVersionUID = -8314702649252239L;
    private static Logger log = LoggerFactory.getLogger(JmsSendAndForgetRuntimeJndiLookups.class);

    @EJB(beanName = "JmsSendAndForgetRuntimeJndiLookupsService")
    private JmsPublisherLocal jmsSimplePublisher;

    @Override
    protected final JmsPublisherLocal getJmsPublisherLocal() {
        return jmsSimplePublisher;
    }
}
