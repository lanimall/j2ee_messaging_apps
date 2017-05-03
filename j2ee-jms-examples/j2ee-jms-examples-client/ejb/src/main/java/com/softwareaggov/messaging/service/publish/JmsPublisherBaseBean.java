package com.softwareaggov.messaging.service.publish;

import com.softwareaggov.messaging.service.utils.CounterSingletonLocal;
import com.softwareaggov.messaging.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import java.util.Map;

/*
 * Simple JMS Publisher bean relying on the connection factory to create the JMS connection on each call
 * Created by fabien.sanglier on 6/15/16.
 */
public abstract class JmsPublisherBaseBean implements JmsPublisherLocal {
    private static Logger log = LoggerFactory.getLogger(JmsPublisherBaseBean.class);

    @EJB
    private CounterSingletonLocal messageProcessingCounter;

    @Resource(name="jmsDeliveryMode")
    private Integer jmsDeliveryMode = null;

    @Resource(name="jmsPriority")
    private Integer jmsPriority = null;

    private transient JMSHelper jmsHelper;

    public abstract ConnectionFactory getJmsConnectionFactory();

    public abstract Destination getJmsDestination();

    @PostConstruct
    private void init(){
        this.jmsHelper = JMSHelper.createSender(getJmsConnectionFactory(), getJmsDestination());
    }

    @PreDestroy
    private void cleanup(){
        if(null != jmsHelper)
            jmsHelper.cleanup();
        jmsHelper = null;
    }

    @TransactionAttribute(value=TransactionAttributeType.NOT_SUPPORTED)
    public String sendTextMessage(final String msgTextPayload, final Map<String,String> msgHeaderProperties) {
        String returnText = "";

        if(log.isDebugEnabled())
            log.debug("in EJB: sendTextMessage");

        try {
            returnText = jmsHelper.sendTextMessage(msgTextPayload, msgHeaderProperties, JMSHelper.generateCorrelationID(), null, jmsDeliveryMode, jmsPriority);

            //increment processing counter
            messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName());
        } catch (JMSException e) {
            messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-errors");
            log.error("JMS Error occurred", e);
            throw new EJBException(e);
        }

        return returnText;
    }
}
