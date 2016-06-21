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

        TextMessage msg = null;
        try {

            if (null != rcvMessage) {
                if (rcvMessage instanceof TextMessage) {
                    msg = (TextMessage) rcvMessage;

                    String msgPayload = msg.getText();

                    //get correlationid from message
                    String correlationId = msg.getJMSCorrelationID();

                    int deliveryMode = msg.getJMSDeliveryMode();
                    int priority = msg.getJMSPriority();

                    //get the replyTo (if not set, let the JMSHelper use the default destination
                    Destination replyTo = msg.getJMSReplyTo();

                    if(log.isDebugEnabled()) {
                        String request = String.format("%s * %s ?[correlationID = %s]",
                                msg.getStringProperty("factor1"),
                                msg.getStringProperty("factor2"),
                                correlationId
                        );

                        log.debug("RequestConsumerReplyBean: Received Message from queue: " + request);
                    }

                    Map<String,String> responseHeaderProperties = new HashMap<String, String>(3);
                    responseHeaderProperties.put("factor1", msg.getStringProperty("factor1"));
                    responseHeaderProperties.put("factor2", msg.getStringProperty("factor2"));

                    //build the response
                    try {
                        int result = performOperation(Integer.parseInt(msg.getStringProperty("factor1")), Integer.parseInt(msg.getStringProperty("factor2")));
                        responseHeaderProperties.put("result", new Integer(result).toString());
                    } catch (NumberFormatException e) {
                        responseHeaderProperties.put("result", "NAN");
                    }

                    if(log.isDebugEnabled()) {
                        String responseText = String.format("%s * %s = %s [correlationID = %s]",
                                responseHeaderProperties.get("factor1"),
                                responseHeaderProperties.get("factor2"),
                                responseHeaderProperties.get("result"),
                                correlationId
                        );

                        log.debug("About to respond with text: " + responseText);
                    }

                    jmsHelper.sendMessage(replyTo, msgPayload, responseHeaderProperties, correlationId, null, deliveryMode, priority);
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