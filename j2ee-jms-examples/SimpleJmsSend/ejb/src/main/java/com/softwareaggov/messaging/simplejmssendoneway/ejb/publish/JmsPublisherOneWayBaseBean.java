package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish;

import com.softwareaggov.messaging.libs.utils.JMSHelper;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.utils.CounterLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class JmsPublisherOneWayBaseBean implements JmsPublisherLocal {
    private static Logger log = LoggerFactory.getLogger(JmsPublisherOneWayBaseBean.class);

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

    private volatile boolean init = false;

    protected transient JMSHelper jmsHelper;

    private transient Destination jmsReplyTo;

    public abstract ConnectionFactory getJmsConnectionFactory();

    public abstract Destination getJmsDestination();

    public void ejbCreate() {
        log.info("ejbCreate()");
        messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-create");
    }

    @PreDestroy
    private void cleanup() {
        log.info("ejbRemove()");
        messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-remove");
        if (null != jmsHelper)
            jmsHelper.cleanup();
        jmsHelper = null;
    }

    private void initialize() {
        if (!init) {
            synchronized (this.getClass()) {
                if (!init) {
                    messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-init");

                    this.jmsHelper = JMSHelper.createSender(getJmsConnectionFactory());

                    //JMS reply destination
                    if (null != jmsReplyDestinationName && !"".equals(jmsReplyDestinationName) &&
                            null != jmsReplyDestinationType && !"".equals(jmsReplyDestinationType)) {
                        try {
                            jmsReplyTo = jmsHelper.lookupDestination(jmsReplyDestinationName, jmsReplyDestinationType);
                        } catch (Exception e) {
                            throw new EJBException(e);
                        }
                    }
                    init = true;
                }
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

        //call initialize (will do something only once...
        initialize();

        try {
            returnText = sendMessage(getJmsDestination(), msgTextPayload, msgHeaderProperties, jmsDeliveryMode, jmsPriority, null, jmsReplyTo);

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
