package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/28/16.
 */

@Stateless(name = "JmsSendAndForgetRuntimeJndiLookupsService")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(JmsPublisherLocal.class)
@Remote(JmsPublisherRemote.class)
public class JmsSendAndForgetRuntimeJndiLookupsBean extends JmsPublisherBase implements JmsPublisherLocal {
    private static Logger log = LoggerFactory.getLogger(JmsSendAndForgetRuntimeJndiLookupsBean.class);

    @PostConstruct
    public void ejbCreate() {
        log.info("ejbCreate()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-create");
    }

    @Override
    public ConnectionFactory getJmsConnectionFactory() {
        return (ConnectionFactory) lookupEnvResource("jms/someManagedCF");
    }

    @Override
    public Destination getJmsDestination() {
        return (Destination) lookupEnvResource("jms/someManagedDestination");
    }

    private Object lookupEnvResource(String jndiLookupName) {
        Object resource = null;
        try {
            // create the context
            final Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            resource = envCtx.lookup(jndiLookupName);
        } catch (NamingException e) {
            log.warn("Could not lookup the resource " + jndiLookupName);
        }
        return resource;
    }

    @Override
    protected String sendMessage(Destination destination, boolean sessionTransacted, int sessionAcknowledgeMode, Object payload, Map<String, Object> headerProperties, Integer deliveryMode, Integer priority, String correlationID, Destination replyTo) throws JMSException {
        return null;
    }
}
