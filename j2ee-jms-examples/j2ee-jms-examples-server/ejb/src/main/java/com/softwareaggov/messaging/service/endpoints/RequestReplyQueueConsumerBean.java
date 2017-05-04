package com.softwareaggov.messaging.service.endpoints;

import com.softwareaggov.messaging.service.processors.RequestReplyProcessingLocal;
import com.softwareaggov.messaging.utils.JMSHelper;
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
@TransactionManagement(value = TransactionManagementType.BEAN)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public class RequestReplyQueueConsumerBean implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602751473208935601L;

    private static Logger log = LoggerFactory.getLogger(RequestReplyQueueConsumerBean.class);

    @Resource(name = "jms/someManagedCF")
    private ConnectionFactory connectionFactory;

    @Resource(name = "jms/someManagedReplyTo")
    private Destination jmsDefaultReplyTo;

    @EJB(beanName = "RequestReplyProcessingBean", beanInterface = RequestReplyProcessingLocal.class)
    private RequestReplyProcessingLocal requestReplyProcessing;

    private transient JMSHelper jmsHelper;
    private transient MessageDrivenContext mdbContext;

    public RequestReplyQueueConsumerBean() {
        super();
    }

    public void ejbRemove() throws EJBException {
        log.info(RequestReplyQueueConsumerBean.class + ":ejbRemove()");
    }

    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        mdbContext = ctx;
    }

    public void ejbCreate() {
        log.info(RequestReplyQueueConsumerBean.class + ":ejbCreate()");
        jmsHelper = JMSHelper.createSender(connectionFactory, jmsDefaultReplyTo);
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message rcvMessage) {
        if(log.isDebugEnabled())
            log.debug(RequestReplyQueueConsumerBean.class + ":onMessage() start");

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
                    Destination replyTo = null;
                    //Destination replyTo = msg.getJMSReplyTo();

                    if(log.isDebugEnabled()) {
                        String request = String.format("%s * %s ?[correlationID = %s]",
                                msg.getStringProperty("factor1"),
                                msg.getStringProperty("factor2"),
                                correlationId
                        );

                        log.debug(RequestReplyQueueConsumerBean.class + ":Received Message from queue: " + request);
                    }

                    //processing the message
                    String result = requestReplyProcessing.performMultiplicationFromStrings(msg.getStringProperty("factor1"), msg.getStringProperty("factor2"));

                    Map<String,String> responseHeaderProperties = new HashMap<String, String>(3);
                    responseHeaderProperties.put("factor1", msg.getStringProperty("factor1"));
                    responseHeaderProperties.put("factor2", msg.getStringProperty("factor2"));
                    responseHeaderProperties.put("result", result);

                    if(log.isDebugEnabled()) {
                        log.debug(
                                "About to respond with text: {}",
                                String.format("%s * %s = %s [correlationID = %s]",
                                        responseHeaderProperties.get("factor1"),
                                        responseHeaderProperties.get("factor2"),
                                        responseHeaderProperties.get("result"),
                                        correlationId
                                )
                        );
                    }

                    jmsHelper.sendTextMessage(replyTo, msgPayload, responseHeaderProperties, correlationId, null, deliveryMode, priority);
                } else {
                    throw new EJBException(RequestReplyQueueConsumerBean.class + ":Message of wrong type: " + rcvMessage.getClass().getName());
                }
            } else {
                if(log.isWarnEnabled())
                    log.warn(RequestReplyQueueConsumerBean.class + ":Received Message from queue: null");
            }
        } catch (JMSException e) {
            throw new EJBException(e);
        }
    }
}