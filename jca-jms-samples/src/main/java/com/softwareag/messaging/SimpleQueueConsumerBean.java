package com.softwareag.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * <p>
 * A simple Message Driven Bean that receives and processes messages through JMS constructs from defined JMS destinations
 * </p>
 *
 * @author Fabien Sanglier
 */
@MessageDriven(name = "SimpleQueueConsumerBean", activationConfig = {
        @ActivationConfigProperty(propertyName = "connectionFactoryJndiName", propertyValue = "SimpleQCF"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destinationJndiName", propertyValue = "simplequeue"),
        @ActivationConfigProperty(propertyName = "maxPoolSize", propertyValue = "50"),
        @ActivationConfigProperty(propertyName = "maxWaitTime", propertyValue = "10"),
        @ActivationConfigProperty(propertyName = "redeliveryAttempts", propertyValue = "10"),
        @ActivationConfigProperty(propertyName = "redeliveryInterval", propertyValue = "1"),
        @ActivationConfigProperty(propertyName = "reconnectAttempts", propertyValue = "10"),
        @ActivationConfigProperty(propertyName = "reconnectInterval", propertyValue = "30")
})

@TransactionManagement(value = TransactionManagementType.BEAN)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
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
        log.info("SimpleQueueConsumerBean: onMessage() start");

        TextMessage msg = null;
        try {
            if (null != rcvMessage) {
                if (rcvMessage instanceof TextMessage) {
                    msg = (TextMessage) rcvMessage;
                    log.info("SimpleQueueConsumerBean: Received Message from queue: " + msg.getText());
                } else {
                    log.error("SimpleQueueConsumerBean: Message of wrong type: " + rcvMessage.getClass().getName());
                }
            } else {
                log.info("SimpleQueueConsumerBean: Received Message from queue: null");
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}