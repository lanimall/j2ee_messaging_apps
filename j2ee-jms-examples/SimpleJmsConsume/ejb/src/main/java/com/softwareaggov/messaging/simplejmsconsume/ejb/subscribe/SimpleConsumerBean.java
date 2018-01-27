package com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe;

import com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe.processor.MessageProcessorLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.jms.MessageListener;

/**
 * <p>
 * A simple Message Driven Bean that receives and processes messages through JMS constructs from defined JMS destinations
 * </p>
 *
 * @author Fabien Sanglier
 */

@MessageDriven
@TransactionManagement(value = TransactionManagementType.BEAN)
public class SimpleConsumerBean extends AbstractConsumeMDB implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602758394560935601L;

    private static Logger log = LoggerFactory.getLogger(SimpleConsumerBean.class);

    //the message processor to use
    @EJB(beanName = "MockSleepBean")
    private MessageProcessorLocal messageProcessor;

    public SimpleConsumerBean() {
        super();
    }

    @Override
    protected MessageProcessorLocal getMessageProcessor() {
        return messageProcessor;
    }
}