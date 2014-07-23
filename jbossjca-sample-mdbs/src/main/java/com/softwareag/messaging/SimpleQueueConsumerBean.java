package com.softwareag.messaging;

import org.jboss.ejb3.annotation.Pool;
import org.jboss.ejb3.annotation.ResourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * A simple Message Driven Bean that asynchronously receives and processes the
 * messages that are sent to the queue.
 * </p>
 * 
 * @author Fabien
 * 
 */
@MessageDriven(name = "SimpleQueueConsumerBean", activationConfig = {
		@ActivationConfigProperty(propertyName = "connectionFactoryJndiName", propertyValue = "InboundQueueConnectionFactory"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destinationJndiName", propertyValue = "simplequeue"),
        @ActivationConfigProperty(propertyName = "maxPoolSize", propertyValue = "50"),
        @ActivationConfigProperty(propertyName = "maxWaitTime", propertyValue = "10"),
        @ActivationConfigProperty(propertyName = "redeliveryAttempts", propertyValue = "10"),
        @ActivationConfigProperty(propertyName = "redeliveryInterval", propertyValue = "1"),
        @ActivationConfigProperty(propertyName = "reconnectAttempts", propertyValue = "-1"),
        @ActivationConfigProperty(propertyName = "reconnectInterval", propertyValue = "5")
})

@TransactionManagement(value=TransactionManagementType.BEAN)
@TransactionAttribute(value=TransactionAttributeType.NOT_SUPPORTED)
@Pool(value="mdb-strict-max-pool")
@ResourceAdapter("webm-jmsra.rar")
public class SimpleQueueConsumerBean implements MessageListener, MessageDrivenBean {
	private static final long serialVersionUID = -4602751473208935601L;

	private static Logger log = LoggerFactory.getLogger(SimpleQueueConsumerBean.class);

	private transient MessageDrivenContext mdbContext;

	public SimpleQueueConsumerBean() {
		super();
		log.info("SimpleQueueConsumerBean: instantiated");

        Properties properties = System.getProperties();
        for (Map.Entry entry : properties.entrySet()) {
            if(((String)entry.getKey()).startsWith("com.webmethods"))
                log.info(String.format("%s = %s", (String) entry.getKey(), (String) entry.getValue()));
        }
	}

	public void ejbRemove() throws EJBException {
        log.info("SimpleQueueConsumerBean: ejbRemove");
	}

	public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
		mdbContext = ctx;
	}

	public void ejbCreate() {
        log.info("SimpleQueueConsumerBean: ejbCreate");
	}

	/**
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message rcvMessage) {
		log.info("SimpleQueueConsumerBean: onMessage() start");

		TextMessage msg = null;
		try {
			if(null != rcvMessage){
				if (rcvMessage instanceof TextMessage) {
					msg = (TextMessage) rcvMessage;
					log.info("SimpleQueueConsumerBean: Received Message from queue: " + msg.getText());
				} else {
                    log.error("SimpleQueueConsumerBean: Message of wrong type: " + rcvMessage.getClass().getName());
				}
			} else {
                log.info("SimpleQueueConsumerBean: Received Message from queue: null");
			}
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}
}