package com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe.processor;

import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsPublisherRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.*;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

@Stateless(name = "SendAndForgetProcessor")
@Local(MessageProcessorLocal.class)
@TransactionManagement(value = TransactionManagementType.BEAN)
public class SendAndForgetBean implements MessageProcessorLocal {
    private static Logger log = LoggerFactory.getLogger(SendAndForgetBean.class);

    @EJB(beanName = "JmsSendAndForgetService", beanInterface = JmsPublisherRemote.class)
    private JmsPublisherRemote jmsMessagePublisher;

    @Override
    public Map.Entry<String, Map<String, String>> processMessage(Message msg) throws JMSException {
        if (msg instanceof TextMessage) {

            TextMessage txtMsg = (TextMessage) msg;

            //copy the properties from the incoming message
            HashMap props = new HashMap();
            Enumeration txtMsgPropertiesEnum = txtMsg.getPropertyNames();
            while (txtMsgPropertiesEnum.hasMoreElements()) {
                String propName = (String) txtMsgPropertiesEnum.nextElement();
                props.put(propName, txtMsg.getObjectProperty(propName));
            }

            //send the send and wait message
            String textReturned = jmsMessagePublisher.sendTextMessage(txtMsg.getText(), Collections.unmodifiableMap(props));

            return new AbstractMap.SimpleImmutableEntry<String, Map<String, String>>(
                    textReturned, null
            );
        } else {
            throw new RuntimeException("Received non-text message");
        }
    }
}