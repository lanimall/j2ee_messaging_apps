package com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe;

import com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe.processor.MessageProcessorLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenBean;
import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;

/**
 * <p>
 * A simple Message Driven Bean that receives and processes messages through JMS constructs from defined JMS destinations
 * </p>
 *
 * @author Fabien Sanglier
 */

@MessageDriven(name = "SimpleConsumerBean")
public class SimpleConsumerBean extends AbstractConsumeMDB implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602758394560935601L;

    private static Logger log = LoggerFactory.getLogger(SimpleConsumerBean.class);

    //the message processor to use
    @EJB(beanName = "MockSleepBean")
    private MessageProcessorLocal messageProcessor;

    @Resource(name = "jms/someReplyManagedCF")
    private ConnectionFactory jmsConnectionFactory;

    public SimpleConsumerBean() {
        super();
    }

    @Override
    protected MessageProcessorLocal getMessageProcessor() {
        return messageProcessor;
    }

    @Override
    public ConnectionFactory getReplyConnectionFactory() {
        return jmsConnectionFactory;
    }
}