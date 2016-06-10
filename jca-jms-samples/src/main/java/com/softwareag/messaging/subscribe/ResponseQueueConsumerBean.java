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

@MessageDriven(name = "ResponseQueueConsumerBean")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public class ResponseQueueConsumerBean implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602751473208935601L;

    private static Logger log = LoggerFactory.getLogger(ResponseQueueConsumerBean.class);

    private transient MessageDrivenContext mdbContext;

    public ResponseQueueConsumerBean() {
        super();
    }

    public void ejbRemove() throws EJBException {
        log.info("ResponseQueueConsumerBean: ejbRemove");
    }

    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        mdbContext = ctx;
    }

    public void ejbCreate() {
        log.info("ResponseQueueConsumerBean: ejbCreate");
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message rcvMessage) {
        if(log.isDebugEnabled())
            log.debug("ResponseQueueConsumerBean: onMessage() start");

        MapMessage msg = null;
        try {
            if (null != rcvMessage) {
                if (rcvMessage instanceof MapMessage) {
                    msg = (MapMessage) rcvMessage;
                    if(log.isInfoEnabled())
                        log.info("ResponseQueueConsumerBean: Received Message from queue: " + msg.getStringProperty(JMSHelper.PAYLOAD_TEXTMSG_PROPERTY));
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