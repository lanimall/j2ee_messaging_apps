package com.softwareaggov.messaging.service.subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * <p>
 * A simple Message Driven Bean that receives and processes messages through JMS constructs from defined JMS destinations
 * </p>
 *
 * @author Fabien Sanglier
 */

@MessageDriven(name = "SimpleTopicConsumerBean")
@TransactionManagement(value = TransactionManagementType.BEAN)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public class SimpleTopicConsumerBean extends BaseProcessingConsumer implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602752342903745601L;

    private static Logger log = LoggerFactory.getLogger(SimpleTopicConsumerBean.class);

    public SimpleTopicConsumerBean() {
        super();
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message rcvMessage) {
        processMessage(rcvMessage);
    }
}