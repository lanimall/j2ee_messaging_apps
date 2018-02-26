package com.softwareaggov.messaging.service.endpoints.jms.mdb;

import com.softwareaggov.messaging.libs.utils.JMSHelper;
import com.softwareaggov.messaging.service.endpoints.jms.mdb.processor.MDBProcessorLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;

/**
 * <p>
 * A simple Message Driven Bean that receives and processes messages through JMS constructs from defined JMS destinations
 * </p>
 *
 * @author Fabien Sanglier
 */

@TransactionManagement(value = TransactionManagementType.BEAN)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public abstract class AbstractConsumeReply implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602751473208935601L;

    private static Logger log = LoggerFactory.getLogger(AbstractConsumeReply.class);

    @Resource(name = "jms/someManagedCF")
    private ConnectionFactory connectionFactory;

    @Resource(name = "jms/someManagedReplyTo")
    private Destination jmsDefaultReplyTo;

    private transient JMSHelper jmsHelper;
    private transient MessageDrivenContext mdbContext;

    //get the implementation for the message processing
    protected abstract MDBProcessorLocal getMessageProcessing();

    public AbstractConsumeReply() {
        super();
    }

    public void ejbRemove() throws EJBException {
        log.info(AbstractConsumeReply.class + ":ejbRemove()");
    }

    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        mdbContext = ctx;
    }

    public void ejbCreate() {
        log.info(AbstractConsumeReply.class + ":ejbCreate()");
        jmsHelper = JMSHelper.createSender(connectionFactory);
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message msg) {
        if (log.isDebugEnabled())
            log.debug(AbstractConsumeReply.class + ":onMessage() start");

        try {
            if (null != msg) {
                int deliveryMode = msg.getJMSDeliveryMode();
                int priority = msg.getJMSPriority();

                //get the replyTo if it's set - if not, try to use a predefined one
                Destination replyTo = msg.getJMSReplyTo();
                if (null == replyTo)
                    replyTo = jmsDefaultReplyTo;

                //get MessageID from message, and set the reply correlationID with it
                String correlationId = msg.getJMSMessageID();

                //processing the message
                MDBProcessorLocal messageProcessing = getMessageProcessing();
                if (null == messageProcessing)
                    throw new EJBException("Message Processing is null...unexpected.");

                //process the message
                Map.Entry<String, Map<String, String>> result = messageProcessing.processMessage(msg);

                String payload = null;
                Map<String, String> headerProperties = null;
                if (null != result) {
                    payload = result.getKey();
                    headerProperties = result.getValue();
                }

                //send reply
                jmsHelper.sendTextMessage(replyTo, payload, headerProperties, deliveryMode, priority, correlationId, null);
            } else {
                if (log.isWarnEnabled())
                    log.warn(AbstractConsumeReply.class + ":Received Message from queue: null");
            }
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }
}