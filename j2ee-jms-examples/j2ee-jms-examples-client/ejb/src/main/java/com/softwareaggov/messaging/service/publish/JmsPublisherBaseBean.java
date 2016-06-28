package com.softwareaggov.messaging.service.publish;

import com.softwareaggov.messaging.service.utils.CounterSingletonLocal;
import com.softwareaggov.messaging.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
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

    private transient JMSHelper jmsHelper;

    public abstract ConnectionFactory getJmsConnectionFactory();

    public abstract Destination getJmsDestination();

    @PostConstruct
    private void init(){
        this.jmsHelper = JMSHelper.createSender(getJmsConnectionFactory(), getJmsDestination());
    }

    @TransactionAttribute(value=TransactionAttributeType.NOT_SUPPORTED)
    public String sendTextMessage(final String msgTextPayload, final Map<String,String> msgHeaderProperties) {
        String returnText = "";

        if(log.isDebugEnabled())
            log.debug("in EJB: sendTextMessage");

        try {
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
