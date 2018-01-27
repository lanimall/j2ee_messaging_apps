package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish;

import com.softwareaggov.messaging.libs.utils.JMSHelper;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.utils.CounterLocal;
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
public abstract class JmsPublisherBase implements JmsPublisherLocal {
    private static Logger log = LoggerFactory.getLogger(JmsPublisherBase.class);

    @EJB
    private CounterLocal messageProcessingCounter;

    @Resource(name = "jmsDeliveryMode")
    private Integer jmsDeliveryMode = null;

    @Resource(name = "jmsPriority")
    private Integer jmsPriority = null;

    @Resource(name = "jmsReplyDestinationName")
    private String jmsReplyDestinationName = null;

    @Resource(name = "jmsReplyDestinationType")
    private String jmsReplyDestinationType = null;

//    private volatile boolean init = false;

    protected transient JMSHelper jmsHelper;

    private transient Destination jmsReplyTo;

    @PostConstruct
    public void ejbCreate() {
        log.info("ejbCreate()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-create");

        jmsReplyTo = null;
        if (null != getJmsConnectionFactory()) {
            this.jmsHelper = JMSHelper.createSender(getJmsConnectionFactory());

            //JMS reply destination
            if (null != jmsReplyDestinationName && !"".equals(jmsReplyDestinationName) &&
                    null != jmsReplyDestinationType && !"".equals(jmsReplyDestinationType)) {
                try {
                    jmsReplyTo = jmsHelper.lookupDestination(jmsReplyDestinationName, jmsReplyDestinationType);
                } catch (Exception e) {
                    log.error("Could not lookup/create the replyTo Destination...Check that the lookup info is accurate!", e);
                }
            }
        }
    }

    @PreDestroy
    public void ejbRemove() throws EJBException {
        log.info("ejbRemove()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-remove");
        jmsReplyTo = null;
//        init = false;
        if (null != jmsHelper)
            jmsHelper.cleanup();
        jmsHelper = null;
    }

// Fabien: Not sure why I had done it this way instead of during PostConstruct stage...
//    private void initialize() {
//        if (!init) {
//            synchronized (this.getClass()) {
//                if (!init) {
//                    messageProcessingCounter.incrementAndGet(getBeanName() + "-init");
//
//                    this.jmsHelper = JMSHelper.createSender(getJmsConnectionFactory());
//
//                    //JMS reply destination
//                    if (null != jmsReplyDestinationName && !"".equals(jmsReplyDestinationName) &&
//                            null != jmsReplyDestinationType && !"".equals(jmsReplyDestinationType)) {
//                        try {
//                            jmsReplyTo = jmsHelper.lookupDestination(jmsReplyDestinationName, jmsReplyDestinationType);
//                        } catch (Exception e) {
//                            throw new EJBException(e);
//                        }
//                    }
//                    init = true;
//                }
//            }
//        }
//    }

    protected String getBeanName() {
        return this.getClass().getSimpleName();
    }

    abstract ConnectionFactory getJmsConnectionFactory();

    abstract Destination getJmsDestination();

    abstract String sendMessage(Destination destination, final String payload, final Map<String, String> headerProperties, Integer deliveryMode, Integer priority, String correlationID, Destination replyTo) throws JMSException;

    @TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
    public String sendTextMessage(final String msgTextPayload, final Map<String, String> msgHeaderProperties) {
        String returnText = "";
        if (log.isDebugEnabled())
            log.debug("in EJB: sendTextMessage");

        //call initialize (will do something only once...
        //initialize();

        try {
            returnText = sendMessage(getJmsDestination(), msgTextPayload, msgHeaderProperties, jmsDeliveryMode, jmsPriority, null, jmsReplyTo);

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
