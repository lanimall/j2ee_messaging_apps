package com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe.processor;

import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsPublisherRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.*;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

@Stateless(name = "SendAndWaitProcessor")
@Local(MessageProcessorLocal.class)
@TransactionManagement(value = TransactionManagementType.BEAN)
public class SendAndWaitBean implements MessageProcessorLocal {
    private static Logger log = LoggerFactory.getLogger(SendAndWaitBean.class);

//    @EJB(beanName = "JmsSendAndWaitService", beanInterface = JmsPublisherRemote.class)
//    private JmsPublisherRemote jmsMessagePublisher;

    // This will be injected dynamically by jndi lookup
    private JmsPublisherRemote jmsMessagePublisher;

    final String appName = "SimpleJmsSend";
    final String moduleName = "SimpleJmsSend-ejb";
    final String beanName = "JmsSendAndWaitService";
    final String viewClassName = JmsPublisherRemote.class.getName();

    @PostConstruct
    public void initialize() {
        final Properties jndiProperties = new Properties();
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");

        // create the context
        final Context context;
        try {
            context = new InitialContext(jndiProperties);
            jmsMessagePublisher = (JmsPublisherRemote) context.lookup("ejb:" + appName + "/" + moduleName + "/" + beanName + "!" + viewClassName);
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Map.Entry<String, Map<String, String>> processMessage(Message msg) throws JMSException {
        if (msg instanceof TextMessage) {

            TextMessage txtMsg = (TextMessage) msg;

            if (null != jmsMessagePublisher) {
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
                return null;
            }
        } else {
            throw new RuntimeException("Received non-text message");
        }
    }
}