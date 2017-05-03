package com.softwareaggov.messaging.service.subscribe;

import com.softwareaggov.messaging.service.utils.CounterSingletonLocal;
import com.softwareaggov.messaging.service.utils.MessageProcessingLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;

/**
 * Created by fabien.sanglier on 3/27/17.
 */
public abstract class BaseProcessingConsumer implements MessageDrivenBean {
    private static Logger log = LoggerFactory.getLogger(BaseProcessingConsumer.class);

    @EJB(beanName = "MessageProcessingBean", beanInterface = MessageProcessingLocal.class)
    private MessageProcessingLocal messageProcessing;

    @EJB
    private CounterSingletonLocal messageProcessingCounter;

    @Resource(name="mockSleepTimeInMillis")
    private Long mockSleepTimeInMillis = 0L;

    @Resource(name="mockExceptionsCountInterval")
    private Integer mockExceptionsCountInterval = 0;

    private transient MessageDrivenContext mdbContext;

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

    protected void processMessage(Message rcvMessage) {
        if(log.isDebugEnabled())
            log.debug("ProcessMessage start");

        try {
            long newCount = messageProcessingCounter.incrementAndGet(this.getClass().getSimpleName() + "-consumed");

            messageProcessing.processSimpleQueueResponseMessage(rcvMessage);

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
