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
public abstract class JmsPublisherSyncWaitBaseBean implements JmsPublisherLocal {
    private static Logger log = LoggerFactory.getLogger(JmsPublisherSyncWaitBaseBean.class);

    @EJB
    private CounterSingletonLocal messageProcessingCounter;

    @Resource(name = "jmsDeliveryMode")
    private Integer jmsDeliveryMode = null;

    @Resource(name = "jmsPriority")
    private Integer jmsPriority = null;

    @Resource(name = "jmsResponseWaitMillis")
    private Integer jmsResponseWaitMillis = null;

    private transient JMSHelper jmsHelper;

    public abstract ConnectionFactory getJmsConnectionFactory();

    public abstract Destination getJmsDestination();

    public abstract Destination getJmsReplyDestination();

    @PostConstruct
    private void init() {
        this.jmsHelper = JMSHelper.createSender(getJmsConnectionFactory());
    }

    @PreDestroy
    private void cleanup() {
        if (null != jmsHelper)
            jmsHelper.cleanup();
        jmsHelper = null;
    }

    @TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
    public String sendTextMessage(final String msgTextPayload, final Map<String, String> msgHeaderProperties) {
        String returnText = "";

        if (log.isDebugEnabled())
            log.debug("in EJB: sendTextMessage");

        try {
            returnText = jmsHelper.sendTextMessageAndWait(getJmsDestination(), msgTextPayload, msgHeaderProperties, jmsDeliveryMode, jmsPriority, getJmsReplyDestination(), jmsResponseWaitMillis);

            //increment processing counter
            messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName());

            if (null == returnText) {
                messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-responseIsNull");
            } else {
                messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-responseNotNull");
            }
        } catch (JMSException e) {
            messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-errors");
            log.error("JMS Error occurred", e);
            throw new EJBException(e);
        }

        return returnText;
    }
}
