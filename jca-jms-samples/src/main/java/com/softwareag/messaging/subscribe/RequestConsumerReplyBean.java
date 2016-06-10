package com.softwareag.messaging.subscribe;

import com.softwareag.messaging.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * A simple Message Driven Bean that receives and processes messages through JMS constructs from defined JMS destinations
 * </p>
 *
 * @author Fabien Sanglier
 */

@MessageDriven(name = "RequestReplyQueueConsumerBean")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public class RequestConsumerReplyBean implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602751473208935601L;

    private static Logger log = LoggerFactory.getLogger(RequestConsumerReplyBean.class);

    @Resource(name = "jms/someManagedQCF")
    private ConnectionFactory connectionFactory;

    @Resource(name = "jms/someManagedResponseQueue")
    private Destination jmsDefaultReplyTo;

    private transient JMSHelper jmsHelper;
    private transient MessageDrivenContext mdbContext;

    public RequestConsumerReplyBean() {
        super();
    }

    public void ejbRemove() throws EJBException {
        log.info("RequestConsumerReplyBean: ejbRemove");
    }

    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        mdbContext = ctx;
    }

    public void ejbCreate() {
        log.info("RequestConsumerReplyBean: ejbCreate");
        jmsHelper = JMSHelper.createSender(connectionFactory, jmsDefaultReplyTo);
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message rcvMessage) {
        if(log.isDebugEnabled())
            log.debug("RequestConsumerReplyBean: onMessage() start");

        MapMessage msg = null;
        try {

            if (null != rcvMessage) {
                if (rcvMessage instanceof MapMessage) {
                    msg = (MapMessage) rcvMessage;

                    if(log.isDebugEnabled())
                        log.debug("RequestConsumerReplyBean: Received Message from queue: " + msg.getStringProperty(JMSHelper.PAYLOAD_TEXTMSG_PROPERTY));

                    //get correlationid from message
                    String correlationId = msg.getJMSCorrelationID();

                    int deliveryMode = msg.getJMSDeliveryMode();

                    //get the replyTo (if not set, let the JMSHelper use the default destination
                    Destination replyTo = msg.getJMSReplyTo();

                    Map<String,String> responsePayload = new HashMap<String, String>(3);
                    responsePayload.put("factor1", msg.getStringProperty("factor1"));
                    responsePayload.put("factor2", msg.getStringProperty("factor2"));

                    //build the response
                    try {
                        int result = performOperation(Integer.parseInt(msg.getStringProperty("factor1")), Integer.parseInt(msg.getStringProperty("factor2")));
                        responsePayload.put("result", new Integer(result).toString());
                    } catch (NumberFormatException e) {
                        responsePayload.put("result", "NAN");
                    }

                    String responseText = String.format("%s * %s = %s [correlationID = %s]",
                            responsePayload.get("factor1"),
                            responsePayload.get("factor2"),
                            responsePayload.get("result"),
                            correlationId
                            );

                    responsePayload.put(JMSHelper.PAYLOAD_TEXTMSG_PROPERTY, responseText);

                    if(log.isDebugEnabled())
                        log.debug("About to respond with text: " + responseText);

                    jmsHelper.sendMessage(replyTo, responsePayload, null, correlationId, null, deliveryMode);
                } else {
                    throw new EJBException("RequestConsumerReplyBean: Message of wrong type: " + rcvMessage.getClass().getName());
                }
            } else {
                if(log.isDebugEnabled())
                    log.debug("RequestConsumerReplyBean: Received Message from queue: null");
            }
        } catch (JMSException e) {
            throw new EJBException(e);
        }
    }

    private int performOperation(int nb1, int nb2){
        return nb1 * nb2;
    }
}