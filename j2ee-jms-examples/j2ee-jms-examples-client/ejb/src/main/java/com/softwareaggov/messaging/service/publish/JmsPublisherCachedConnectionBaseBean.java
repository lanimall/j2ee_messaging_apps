package com.softwareaggov.messaging.service.publish;

import com.softwareaggov.messaging.service.utils.CounterSingletonLocal;
import com.softwareaggov.messaging.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.*;
import java.util.Map;

/*
 * A JMS Publisher Bean that creates a "durable" connection that stays open over the life of this session bean
 * Created by fabien.sanglier on 6/15/16.
 */

public abstract class JmsPublisherCachedConnectionBaseBean implements JmsPublisherLocal {
    private static Logger log = LoggerFactory.getLogger(JmsPublisherCachedConnectionBaseBean.class);

    @EJB
    private CounterSingletonLocal messageProcessingCounter;

    private transient Connection connection;
    private transient JMSHelper jmsHelper;

    public abstract ConnectionFactory getJmsConnectionFactory();

    public abstract Destination getJmsDestination();

    public JmsPublisherCachedConnectionBaseBean() {
    }

    @PostConstruct
    private void init(){
        try {
            checkAndCreateConnection();
        } catch (JMSException e) {
            log.error("error creating the jms connection");
            throw new EJBException(e);
        }

        this.jmsHelper = JMSHelper.createSender(getJmsConnectionFactory(), getJmsDestination());
    }

    @PreDestroy
    private void cleanup(){
        try {
            if(null != connection)
                this.connection.close();
        } catch (JMSException e) {
            log.error("JMS Error occurred", e);
            throw new EJBException(e);
        }
    }

    private synchronized void checkAndCreateConnection() throws JMSException {
        if(null == this.connection)
            this.connection = getJmsConnectionFactory().createConnection();
    }

    @TransactionAttribute(value=TransactionAttributeType.NOT_SUPPORTED)
    public String sendTextMessage(final String msgTextPayload, final Map<String,String> msgHeaderProperties) {
        String returnText = "";
        if(log.isDebugEnabled())
            log.debug("in EJB: sendTextMessage");

        try {
            //make sure the single connection in this bean is created already, if not create
            checkAndCreateConnection();
            returnText = jmsHelper.sendTextMessage(msgTextPayload, msgHeaderProperties, JMSHelper.generateCorrelationID(), null, DeliveryMode.NON_PERSISTENT, 4);

            //increment processing counter
            messageProcessingCounter.increment(this.getClass().getSimpleName());
        } catch (JMSException e) {
            log.error("JMS Error occurred", e);
            throw new EJBException(e);
        }
        return returnText;
    }
}
