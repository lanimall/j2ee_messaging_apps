package com.softwareaggov.messaging.simplejmssendoneway.web.compareTests;

import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsPublisherLocal;
import com.softwareaggov.messaging.simplejmssendoneway.web.BaseMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;

/**
 * <p>
 * A simplistic (on purpose) servlet that sends JMS messages without  JCA construct (no connection pooling and the likes...)
 * </p>
 *
 * @author Fabien Sanglier
 */
@WebServlet("/JmsSendAndForgetNonJCA")
public class JmsSendAndForgetNonJCA extends BaseMessageProducer {
    private static final long serialVersionUID = 1L;
    private static Logger log = LoggerFactory.getLogger(JmsSendAndForgetNonJCA.class);

    @EJB(beanName = "JmsSendAndForgetNonJCATestService")
    private JmsPublisherLocal jmsSimplePublisher;

    @Override
    protected final JmsPublisherLocal getJmsPublisherLocal() {
        return jmsSimplePublisher;
    }
}
