package com.softwareaggov.messaging.service.subscribe;

import com.softwareaggov.messaging.service.utils.CounterSingletonLocal;
import com.softwareaggov.messaging.service.utils.MessageProcessingLocal;
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

@MessageDriven(name = "ResponseQueueConsumerBean")
@TransactionManagement(value = TransactionManagementType.BEAN)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public class ResponseQueueConsumerBean implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602751473208935601L;

    private static Logger log = LoggerFactory.getLogger(ResponseQueueConsumerBean.class);

    @EJB(beanName = "MessageProcessingBean", beanInterface = MessageProcessingLocal.class)
    private MessageProcessingLocal messageProcessing;

    @EJB
    private CounterSingletonLocal messageProcessingCounter;

    private transient MessageDrivenContext mdbContext;

    public ResponseQueueConsumerBean() {
        super();
    }

    public void ejbRemove() throws EJBException {
        log.info("ResponseQueueConsumerBean: ejbRemove");
        messageProcessingCounter.increment(this.getClass().getSimpleName() + "-remove");
    }

    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        mdbContext = ctx;
    }

    public void ejbCreate() {
        log.info("ResponseQueueConsumerBean: ejbCreate");
        messageProcessingCounter.increment(this.getClass().getSimpleName() + "-create");
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message rcvMessage) {
        if(log.isDebugEnabled())
            log.debug("ResponseQueueConsumerBean: onMessage() start");

        TextMessage msg = null;
        try {
            if (null != rcvMessage) {
                if (rcvMessage instanceof TextMessage) {
                    msg = (TextMessage) rcvMessage;
                    String messageProperties = messageProcessing.stringifyMessageProperties(msg, "factor1", "factor2", "result");

//                    String responseText = String.format("%s * %s = %s [correlationID = %s]",
//                            msg.getStringProperty("factor1"),
//                            msg.getStringProperty("factor2"),
//                            msg.getStringProperty("result"),
//                            msg.getJMSCorrelationID()
//                    );

                    //increment processing counter
                    messageProcessingCounter.increment(this.getClass().getSimpleName());

                    messageProperties = String.format("%s [correlationID = %s]",
                            messageProperties,
                            msg.getJMSCorrelationID()
                    );

                    if(log.isInfoEnabled())
                        log.info("ResponseQueueConsumerBean: Received Message from queue with header properties: {}",
                                messageProperties
                        );

//                    if(log.isInfoEnabled())
//                        log.info("ResponseQueueConsumerBean: Received Message from queue with response: " + responseText);
                } else {
                    throw new EJBException("ResponseQueueConsumerBean: Message of wrong type: " + rcvMessage.getClass().getName());
                }
            } else {
                if(log.isInfoEnabled())
                    log.info("ResponseQueueConsumerBean: Received Message from queue: null");
            }
        } catch (JMSException e) {
            throw new EJBException(e);
        }
    }
}