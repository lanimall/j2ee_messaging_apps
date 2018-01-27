package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.compareTests;

import com.softwareaggov.messaging.libs.utils.JMSHelper;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsPublisherLocal;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.utils.CounterLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by fabien.sanglier on 10/12/17.
 */
@Stateless(mappedName = "JmsSendAndForgetNonJCATestBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(JmsPublisherLocal.class)
public class JmsSendAndForgetNonJCATestBean implements JmsPublisherLocal {
    private static Logger log = LoggerFactory.getLogger(JmsSendAndForgetNonJCATestBean.class);

    @EJB
    private CounterLocal messageProcessingCounter;

    @Resource(name = "jms.jndi.contextfactory")
    private String jndiContextFactory = null;

    @Resource(name = "jms.jndi.connection.url")
    private String jndiConnectionUrl = null;

    @Resource(name = "jms.connectionfactory.name")
    private String jmsConnectionFactoryName = null;

    @Resource(name = "jms.default.destination.name")
    private String jmsDefaultDestinationName = null;

    @Resource(name = "jmsDeliveryMode")
    private Integer jmsDeliveryMode = null;

    @Resource(name = "jmsPriority")
    private Integer jmsPriority = null;

    @Resource(name = "jmsReplyDestinationName")
    private String jmsReplyDestinationName = null;

    @Resource(name = "jmsReplyDestinationType")
    private String jmsReplyDestinationType = null;

    protected transient JMSHelper jmsHelper;
    private transient Destination jmsReplyTo;

    private ConnectionFactory connectionFactory;
    private Destination defaultDestination;

    public void ejbCreate() {
        log.info("ejbCreate()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-create");
    }

    @PreDestroy
    private void cleanup() {
        log.info("ejbRemove()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-remove");
        jmsReplyTo = null;
        connectionFactory = null;
        defaultDestination = null;

        if (null != jmsHelper)
            jmsHelper.cleanup();
        jmsHelper = null;
    }

    protected String getBeanName() {
        return this.getClass().getSimpleName();
    }

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

        this.jmsHelper = JMSHelper.createSender(connectionFactory);

        //JMS reply destination
        jmsReplyTo = null;
        if (null != jmsReplyDestinationName && !"".equals(jmsReplyDestinationName) &&
                null != jmsReplyDestinationType && !"".equals(jmsReplyDestinationType)) {
            try {
                jmsReplyTo = jmsHelper.lookupDestination(jmsReplyDestinationName, jmsReplyDestinationType);
            } catch (Exception e) {
                throw new EJBException(e);
            }
        }
    }

    protected String sendMessage(Destination destination, final String payload, final Map<String, String> headerProperties, Integer deliveryMode, Integer priority, String correlationID, Destination replyTo) throws JMSException {
        return jmsHelper.sendTextMessage(destination, payload, headerProperties, deliveryMode, priority, correlationID, replyTo);
    }

    @TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
    public String sendTextMessage(final String msgTextPayload, final Map<String, String> msgHeaderProperties) {
        String returnText = "";
        if (log.isDebugEnabled())
            log.debug("in EJB: sendTextMessage");

        try {
            returnText = sendMessage(defaultDestination, msgTextPayload, msgHeaderProperties, jmsDeliveryMode, jmsPriority, null, jmsReplyTo);

            //increment processing counter
            messageProcessingCounter.incrementAndGet(getBeanName());

            if (null == returnText) {
                messageProcessingCounter.incrementAndGet(getBeanName() + "-responseIsNull");
            } else {
                messageProcessingCounter.incrementAndGet(getBeanName() + "-responseNotNull");
            }
        } catch (JMSException e) {
            messageProcessingCounter.incrementAndGet(getBeanName() + "-errors");
            log.error("JMS Error occurred", e);
            throw new EJBException(e);
        }

        return returnText;
    }
}
