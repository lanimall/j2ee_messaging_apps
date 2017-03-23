package com.softwareaggov.messaging.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
@Local(MessageProcessingLocal.class)
public class MessageProcessingBean implements MessageProcessingLocal {
    private static Logger log = LoggerFactory.getLogger(MessageProcessingBean.class);

    private String stringifyMessageProperties(Message msg, String ... propNames) {
        if(log.isDebugEnabled())
            log.debug("MessageProcessingLocal: stringifyMessageProperties()");

        StringBuilder logText = new StringBuilder();
        try {
            if(null != propNames) {
                for (String propName : propNames) {
                    if (logText.length() > 0) logText.append(",");
                    logText.append(String.format("JMS_Property[%s]=%s", propName, (null != msg.getObjectProperty(propName)) ? msg.getObjectProperty(propName).toString() : "null"));
                }
            }
        } catch (JMSException e) {
            log.warn("Cannot read the message properties...", e);
        }
        return logText.toString();
    }

    @Override
    public void processReqReplyResponseMessage(Message rcvMessage){
        try {
            if (null != rcvMessage) {
                if (rcvMessage instanceof TextMessage) {
                    TextMessage msg = (TextMessage) rcvMessage;
                    String messageProperties = stringifyMessageProperties(msg, "factor1", "factor2", "result");

                    messageProperties = String.format("%s [correlationID = %s]",
                            messageProperties,
                            msg.getJMSCorrelationID()
                    );

                    if(log.isDebugEnabled())
                        log.debug("Received Message from queue with header properties: {}",
                                messageProperties
                        );

                    if(log.isDebugEnabled()){
                        String responseText = String.format("%s * %s = %s [correlationID = %s]",
                                msg.getStringProperty("factor1"),
                                msg.getStringProperty("factor2"),
                                msg.getStringProperty("result"),
                                msg.getJMSCorrelationID()
                        );
                        log.debug("Received Message from queue with response: " + responseText);
                    }
                } else {
                    throw new EJBException("Message of wrong type: " + rcvMessage.getClass().getName());
                }
            } else {
                if(log.isWarnEnabled())
                    log.warn("Received Message from queue: null");
            }
        } catch (Exception e) {
            log.error("Unexpected Exception...", e);
            throw new EJBException(e);
        }
    }

    @Override
    public void processSimpleQueueResponseMessage(Message rcvMessage){
        try {
            if (null != rcvMessage) {
                if (rcvMessage instanceof TextMessage) {
                    TextMessage msg = (TextMessage) rcvMessage;
                    String messageProperties = stringifyMessageProperties(msg, "number_property");

                    if(log.isDebugEnabled())
                        log.debug("Received Message from queue with header properties: {}",
                                messageProperties
                        );
                } else {
                    throw new EJBException("Message of wrong type: " + rcvMessage.getClass().getName());
                }
            } else {
                if(log.isWarnEnabled())
                    log.warn("Received Message from queue: null");
            }
        } catch (Exception e) {
            log.error("Unexpected Exception...", e);
            throw new EJBException(e);
        }
    }
}