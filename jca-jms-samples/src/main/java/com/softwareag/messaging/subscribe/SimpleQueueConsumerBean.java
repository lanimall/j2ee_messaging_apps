package com.softwareag.messaging.subscribe;

import com.softwareag.messaging.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.jms.*;

/**
 * <p>
 * A simple Message Driven Bean that receives and processes messages through JMS constructs from defined JMS destinations
 * </p>
 *
 * @author Fabien Sanglier
 */

//
//@MessageDriven(name = "SimpleQueueConsumerBean", activationConfig = {
//        //@ActivationConfigProperty(propertyName = "connectionFactoryJndiName", propertyValue = "SimpleQCF"),
//        @ActivationConfigProperty(propertyName = "connectionFactoryJndiName", propertyValue = "SSLSimpleQCF"),
//        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
//        @ActivationConfigProperty(propertyName = "destinationJndiName", propertyValue = "simplequeue"),
//        @ActivationConfigProperty(propertyName = "maxPoolSize", propertyValue = "50"),
//        @ActivationConfigProperty(propertyName = "maxWaitTime", propertyValue = "10"),
//        @ActivationConfigProperty(propertyName = "redeliveryAttempts", propertyValue = "10"),
//        @ActivationConfigProperty(propertyName = "redeliveryInterval", propertyValue = "1"),
//        @ActivationConfigProperty(propertyName = "reconnectAttempts", propertyValue = "10"),
//        @ActivationConfigProperty(propertyName = "reconnectInterval", propertyValue = "30")
//})

@MessageDriven(name = "SimpleQueueConsumerBean")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
//@ResourceAdapter("um97fix3-jmsra-trunkpatched.rar")
//@ResourceAdapter("umra_97.rar")
//@ResourceAdapter("umra_97fix8.rar")
public class SimpleQueueConsumerBean implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602751473208935601L;

    private static Logger log = LoggerFactory.getLogger(SimpleQueueConsumerBean.class);

    private transient MessageDrivenContext mdbContext;

    public SimpleQueueConsumerBean() {
        super();
        log.info("SimpleQueueConsumerBean: instantiated");
    }

    public void ejbRemove() throws EJBException {
        log.info("SimpleQueueConsumerBean: ejbRemove");
    }

    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        mdbContext = ctx;
    }

    public void ejbCreate() {
        log.info("SimpleQueueConsumerBean: ejbCreate");
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message rcvMessage) {
        if(log.isDebugEnabled())
            log.debug("SimpleQueueConsumerBean: onMessage() start");

        MapMessage msg = null;
        try {
            if (null != rcvMessage) {
                if (rcvMessage instanceof MapMessage) {
                    msg = (MapMessage) rcvMessage;
                    if(log.isInfoEnabled())
                        log.info("SimpleQueueConsumerBean: Received Message from queue: " + msg.getStringProperty(JMSHelper.PAYLOAD_TEXTMSG_PROPERTY));
                } else {
                    throw new EJBException("SimpleQueueConsumerBean: Message of wrong type: " + rcvMessage.getClass().getName());
                }
            } else {
                if(log.isInfoEnabled())
                    log.info("SimpleQueueConsumerBean: Received Message from queue: null");
            }
        } catch (JMSException e) {
            throw new EJBException(e);
        }
    }
}