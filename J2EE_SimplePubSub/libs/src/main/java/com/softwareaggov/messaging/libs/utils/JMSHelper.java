package com.softwareaggov.messaging.libs.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.*;
import java.util.Map.Entry;

public class JMSHelper {
    private static Logger log = LoggerFactory.getLogger(JMSHelper.class);
    private ConnectionFactory connectionFactory;

    public static String DESTINATION_TYPE_QUEUE = "queue";
    public static String DESTINATION_TYPE_TOPIC = "topic";

    static public enum DestinationType {
        QUEUE,
        TOPIC;

        static public DestinationType parse(String destinationTypeName) throws IllegalArgumentException {
            JMSHelper.DestinationType destinationTypeLocal = QUEUE;
            if ("topic".equalsIgnoreCase(destinationTypeName)) {
                destinationTypeLocal = JMSHelper.DestinationType.TOPIC;
            } else if ("queue".equalsIgnoreCase(destinationTypeName)) {
                destinationTypeLocal = JMSHelper.DestinationType.QUEUE;
            } else {
                throw new IllegalArgumentException("value for destinationTypeName not valid - " + ((null != destinationTypeName) ? destinationTypeName : "null"));
            }
            return destinationTypeLocal;
        }
    }

    private JMSHelper(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void cleanup() {
        connectionFactory = null;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public String sendTextMessage(Destination destination, final String payload, final Map<String, Object> headerProperties) throws JMSException {
        return sendTextMessage(destination, payload, headerProperties, DeliveryMode.PERSISTENT, 4, null, null);
    }

    public String sendTextMessage(Destination destination, final String payload, final Map<String, Object> headerProperties, Integer deliveryMode, Integer priority, String correlationID, Destination replyTo) throws JMSException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            if (null == connectionFactory)
                throw new JMSException("connection factory is null...can't do anything...");

            connection = connectionFactory.createConnection(); // Create connection
            boolean transacted = false;
            session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE); // Create Session

            if (null == destination)
                throw new JMSException("Destination is null...can't do anything...");

            // Create Message Producer
            producer = session.createProducer(destination);

            if (null != deliveryMode && deliveryMode >= 0)
                producer.setDeliveryMode(deliveryMode);

            if (null != priority && priority >= 0)
                producer.setPriority(priority);

            // Create Message
            TextMessage msg = session.createTextMessage();
            if (null != payload)
                msg.setText(payload);

            if (null != replyTo)
                msg.setJMSReplyTo(replyTo);

            if (null != correlationID)
                msg.setJMSCorrelationID(correlationID);

            if (null != headerProperties) {
                for (Entry<String, Object> entry : headerProperties.entrySet()) {
                    msg.setObjectProperty(entry.getKey(), entry.getValue());
                }
            }

            producer.send(msg); // Send Message

            if (log.isDebugEnabled())
                log.debug("message sent successfully");

            return msg.getJMSMessageID();
        } finally {
            if (null != producer)
                producer.close();

            if (null != session)
                session.close();

            if (null != connection)
                connection.close();
        }
    }

    public String sendTextMessageAndWait(Destination destination, final String payload, final Map<String, Object> headerProperties, Integer deliveryMode, Integer priority, Destination replyTo, Long replyWaitTimeoutMs) throws JMSException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        MessageConsumer responseConsumer = null;

        try {
            if (null == connectionFactory)
                throw new JMSException("connection factory is null...can't do anything...");

            connection = connectionFactory.createConnection(); // Create connection
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE); // Create Session

            if (null == destination)
                throw new JMSException("Destination is null...can't do anything...");

            //here, we could fall back on creating a temp response queue if reply is not specified...likely a good thing to try
            if (null == replyTo)
                throw new JMSException("ReplyTo Destination is null...can't do anything...");

            // Create Message Producer
            producer = session.createProducer(destination);
            if (null != deliveryMode && deliveryMode >= 0)
                producer.setDeliveryMode(deliveryMode);

            if (null != priority && priority >= 0)
                producer.setPriority(priority);

            // Create Message
            TextMessage msg = session.createTextMessage();

            if (null != payload)
                msg.setText(payload);

            if (null != replyTo)
                msg.setJMSReplyTo(replyTo);

            if (null != headerProperties) {
                for (Entry<String, Object> entry : headerProperties.entrySet()) {
                    msg.setObjectProperty(entry.getKey(), entry.getValue());
                }
            }

            //send the message
            producer.send(msg); // Send Message

            if (log.isDebugEnabled())
                log.debug("message sent successfully");

            // Create a selector to only get the reply message that matches my request message id.
            String selector = String.format("JMSCorrelationID='%s'", msg.getJMSMessageID());

            //create a consumer with the selector
            responseConsumer = session.createConsumer(replyTo, selector);

            // Start the connection
            connection.start();

            //now wait for the response with a timeout (if null or <0, wait infinite -- 0 means infinite wait)
            if (null == replyWaitTimeoutMs || replyWaitTimeoutMs <= 0) replyWaitTimeoutMs = 0L;
            Message receivedMessage = responseConsumer.receive(replyWaitTimeoutMs);

            //if null, it means no message came within the timeout (or consumer got closed concurrently)
            if (null != receivedMessage && receivedMessage instanceof TextMessage) {
                return ((TextMessage) receivedMessage).getText();
            } else {
                if (null == receivedMessage) {
                    throw new JMSException(String.format("Didn't receive message response within timeout [%d] for Message ID [%s]", replyWaitTimeoutMs, msg.getJMSMessageID()));
                } else {
                    throw new JMSException(String.format("Message response type not expected - received: [%s] / expected: [%s]", receivedMessage.getClass().getName(), TextMessage.class.getName()));
                }
            }
        } finally {
            if (null != producer)
                producer.close();

            if (null != responseConsumer)
                responseConsumer.close();

            if (null != session)
                session.close();

            if (null != connection)
                connection.close();
        }
    }

    public Destination lookupTopicDestination(String destinationName) throws JMSException {
        return lookupDestination(destinationName, DestinationType.TOPIC);
    }

    public Destination lookupQueueDestination(String destinationName) throws JMSException {
        return lookupDestination(destinationName, DestinationType.QUEUE);
    }

    public Destination lookupDestination(String destinationName, String destinationType) throws JMSException {
        return lookupDestination(destinationName, JMSHelper.DestinationType.parse(destinationType));
    }

    public Destination lookupDestination(String destinationName, DestinationType destinationType) throws JMSException {
        Connection connection = null;
        Session session = null;
        Destination destinationLocal = null;

        if (null == connectionFactory)
            throw new JMSException("connection factory is null...can't do anything...");

        try {
            connection = connectionFactory.createConnection(); // Create connection
            boolean transacted = false;
            session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE); // Create Session

            if (!"".equals(destinationName)) {
                if (null != destinationType) {
                    if (destinationType == DestinationType.QUEUE)
                        destinationLocal = session.createQueue(destinationName);
                    else if (destinationType == DestinationType.TOPIC)
                        destinationLocal = session.createTopic(destinationName);
                } else {
                    throw new JMSException("destination type should be defined if using the destinationName construct");
                }
            } else {
                throw new JMSException("destination name is null...can't do anything...");
            }
        } finally {
            if (null != session)
                session.close();

            if (null != connection)
                connection.close();
        }
        return destinationLocal;
    }

    public static Object lookupJNDI(Hashtable<String, String> jndiEnv, final String lookupName) throws NamingException {
        Object jndiLookup = null;
        if (null == jndiEnv) {
            throw new IllegalArgumentException("jndi params not defined.");
        }

        if (log.isDebugEnabled()) {
            for (String key : jndiEnv.keySet()) {
                log.debug(String.format("Context: %s - %s", key.toString(), jndiEnv.get(key)));
            }
        }

        if (null == lookupName)
            throw new IllegalArgumentException("lookupName not defined.");

        // Lookup Connection Factory
        Context namingContext = new InitialContext(jndiEnv);

        if (log.isDebugEnabled())
            log.debug("Context Created : " + namingContext.toString());

        jndiLookup = namingContext.lookup(lookupName);

        if (null != jndiLookup) {
            if (log.isDebugEnabled())
                log.debug("Lookup JNDI Success - Impl Class: {}" + jndiLookup.getClass().getName());
        } else {
            throw new NamingException("Lookup JNDI failed - object name could not be found");
        }

        return jndiLookup;
    }

    public static JMSHelper createSender(ConnectionFactory connectionFactory) {
        return new JMSHelper(connectionFactory);
    }

    public static JMSHelper createSender(Hashtable<String, String> jndiEnv, String connectionFactoryName) throws NamingException, JMSException {
        JMSHelper msgSender = null;
        Object connectionFactory = JMSHelper.lookupJNDI(jndiEnv, connectionFactoryName);

        if (null != connectionFactory && connectionFactory instanceof ConnectionFactory) {
            msgSender = new JMSHelper((ConnectionFactory) connectionFactory);
        } else {
            throw new JMSException("Unexpected connectionFactory object returned from JNDI");
        }

        return msgSender;
    }

    public static String generateCorrelationID() {
        return UUID.randomUUID().toString();
    }

    public static Object getMessagePayload(Message msg) throws JMSException {
        String messagePayload = null;
        if (null != msg) {
            if (msg instanceof TextMessage) {
                messagePayload = ((TextMessage) msg).getText();
            } else if (msg instanceof MapMessage) {
                throw new UnsupportedOperationException("Not implemented yet");
            } else if (msg instanceof BytesMessage) {
                throw new UnsupportedOperationException("Not implemented yet");
            } else if (msg instanceof ObjectMessage) {
                throw new UnsupportedOperationException("Not implemented yet");
            } else if (msg instanceof StreamMessage) {
                throw new UnsupportedOperationException("Not implemented yet");
            }
        }

        return messagePayload;
    }

    public static Map<String, Object> getMessageProperties(Message msg) throws JMSException {
        Map<String, Object> props = null;
        if (null != msg) {
            props = new HashMap();
            Enumeration txtMsgPropertiesEnum = msg.getPropertyNames();
            while (txtMsgPropertiesEnum.hasMoreElements()) {
                String propName = (String) txtMsgPropertiesEnum.nextElement();
                props.put(propName, msg.getObjectProperty(propName));
            }
        }

        return props;
    }
}
