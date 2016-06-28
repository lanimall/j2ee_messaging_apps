package com.softwareaggov.messaging.service.subscribe;

import com.softwareaggov.messaging.service.utils.CounterSingletonLocal;
import com.softwareaggov.messaging.service.utils.MessageProcessingLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
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

@MessageDriven(name = "SimpleQueueConsumerBean")
@TransactionManagement(value = TransactionManagementType.BEAN)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public class SimpleQueueConsumerBean implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602751473208935601L;

    private static Logger log = LoggerFactory.getLogger(SimpleQueueConsumerBean.class);

    @EJB(beanName = "MessageProcessingBean", beanInterface = MessageProcessingLocal.class)
    private MessageProcessingLocal messageProcessing;

    @EJB
    private CounterSingletonLocal messageProcessingCounter;

    private transient MessageDrivenContext mdbContext;

    public SimpleQueueConsumerBean() {
        super();
        log.info("SimpleQueueConsumerBean: instantiated");
    }

    public void ejbRemove() throws EJBException {
        log.info("SimpleQueueConsumerBean: ejbRemove");
        messageProcessingCounter.increment(this.getClass().getSimpleName() + "-remove");
    }

    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        mdbContext = ctx;
    }

    public void ejbCreate() {
        log.info("SimpleQueueConsumerBean: ejbCreate");
        messageProcessingCounter.increment(this.getClass().getSimpleName() + "-create");
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message rcvMessage) {
        if(log.isDebugEnabled())
            log.debug("SimpleQueueConsumerBean: onMessage() start");

        TextMessage msg = null;
        if (null != rcvMessage) {
            if (rcvMessage instanceof TextMessage) {
                msg = (TextMessage) rcvMessage;
                String messageProperties = messageProcessing.stringifyMessageProperties(msg, "number_property");

                //increment processing counter
                messageProcessingCounter.increment(this.getClass().getSimpleName());

                if(log.isInfoEnabled())
                    log.info("SimpleQueueConsumerBean: Received Message from queue with header properties: {}",
                            messageProperties
                            );
            } else {
                throw new EJBException("SimpleQueueConsumerBean: Message of wrong type: " + rcvMessage.getClass().getName());
            }
        } else {
            if(log.isInfoEnabled())
                log.info("SimpleQueueConsumerBean: Received Message from queue: null");
        }
    }
}