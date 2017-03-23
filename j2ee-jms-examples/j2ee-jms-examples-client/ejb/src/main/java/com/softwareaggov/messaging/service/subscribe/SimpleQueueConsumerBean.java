package com.softwareaggov.messaging.service.subscribe;

import com.softwareaggov.messaging.service.utils.CounterSingletonLocal;
import com.softwareaggov.messaging.service.utils.MessageProcessingLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
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

    @Resource(name="mockSleepTimeInMillis")
    private Long mockSleepTimeInMillis = 0L;

    @Resource(name="mockExceptionsCountInterval")
    private Integer mockExceptionsCountInterval = 0;

    private transient MessageDrivenContext mdbContext;

    public SimpleQueueConsumerBean() {
        super();
    }

    public void ejbRemove() throws EJBException {
        log.info("ejbRemove()");
        messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-remove");
    }

    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        mdbContext = ctx;
    }

    public void ejbCreate() {
        log.info("ejbCreate()");
        messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-create");
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message rcvMessage) {
        if(log.isDebugEnabled())
            log.debug("onMessage() start");
        try {
            messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-consumed");

            messageProcessing.processSimpleQueueResponseMessage(rcvMessage);

            //add to single total counter for this main class
            long newCount = messageProcessingCounter.incrementAndGet(SimpleQueueConsumerBean.class.getSimpleName() + "-consumed-totals");
            if(null != mockSleepTimeInMillis && mockSleepTimeInMillis > 0) {
                log.debug("Sleeping " + mockSleepTimeInMillis + " to mock processing time...");
                Thread.sleep(mockSleepTimeInMillis);
            }

            if(null != mockExceptionsCountInterval && mockExceptionsCountInterval > 0) {
                if (newCount % mockExceptionsCountInterval == 0) {
                    throw new EJBException("This is a mocked exception to mock failed processing");
                }
            }
            messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-processed");
        } catch (Exception exc) {
            messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-processing-errors");
            throw new EJBException(exc);
        }
    }
}