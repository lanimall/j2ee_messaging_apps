package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish;

import com.softwareaggov.messaging.libs.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * Created by fabien.sanglier on 10/12/17.
 */
@Stateless(mappedName = "JMSNonJCAOneWayPublisherBean")
@TransactionManagement(TransactionManagementType.CONTAINER)
@Local(JmsPublisherLocal.class)
public class JMSNonJCAOneWayPublisherBean extends JmsPublisherOneWayBaseBean implements JmsPublisherLocal {
    private static Logger log = LoggerFactory.getLogger(JMSNonJCAOneWayPublisherBean.class);

    @Resource(name = "jms.jndi.contextfactory")
    private String jndiContextFactory = null;

    @Resource(name = "jms.jndi.connection.url")
    private String jndiConnectionUrl = null;

    @Resource(name = "jms.connectionfactory.name")
    private String jmsConnectionFactoryName = null;

    @Resource(name = "jms.default.destination.name")
    private String jmsDefaultDestinationName = null;

    private ConnectionFactory connectionFactory;
    private Destination defaultDestination;

    @PostConstruct
    protected void initialize() {
        //JNDI params
        if (null == jndiContextFactory)
            throw new IllegalArgumentException("jms.jndi.contextfactory not defined.");

        if (null == jndiConnectionUrl)
            throw new IllegalArgumentException("jms.jndi.connection.url not defined.");

        Hashtable<String, String> jndiEnv = new Hashtable<String, String>();
        jndiEnv.put("java.naming.factory.initial", jndiContextFactory);

        if (null != jndiConnectionUrl && !"".equals(jndiConnectionUrl)) {
            //add the proptocol if not set -- default to nsp
            if (-1 == jndiConnectionUrl.indexOf("://"))
                jndiConnectionUrl = "nsp://" + jndiConnectionUrl;

            jndiEnv.put("java.naming.provider.url", jndiConnectionUrl);
        }

        //JMS connection factory
        if (null == jmsConnectionFactoryName)
            throw new IllegalArgumentException("jms.connectionfactory.name not defined.");

        try {
            connectionFactory = (ConnectionFactory) JMSHelper.lookupJNDI(jndiEnv, jmsConnectionFactoryName);
        } catch (NamingException e) {
            throw new EJBException(e);
        } catch (ClassCastException e) {
            throw new EJBException("Unexpected connectionFactory object returned from JNDI", e);
        }

        //set the default destination only if it's not set already
        if (null != jmsDefaultDestinationName) {
            try {
                defaultDestination = (Destination) JMSHelper.lookupJNDI(jndiEnv, jmsDefaultDestinationName);
            } catch (NamingException e) {
                throw new EJBException(e);
            } catch (ClassCastException e) {
                throw new EJBException("Unexpected destination object returned from JNDI", e);
            }
        }
    }

    @Override
    public ConnectionFactory getJmsConnectionFactory() {
        return connectionFactory;
    }

    @Override
    public Destination getJmsDestination() {
        return defaultDestination;
    }
}
