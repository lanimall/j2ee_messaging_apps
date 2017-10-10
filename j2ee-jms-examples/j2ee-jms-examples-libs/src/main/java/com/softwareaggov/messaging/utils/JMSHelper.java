package com.softwareaggov.messaging.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class JMSHelper {
    private static Logger log = LoggerFactory.getLogger(JMSHelper.class);

    private ConnectionFactory connectionFactory;
    private Destination defaultDestination;
    private String defaultDestinationName;
    private DestinationType defaultDestinationType;

    public enum DestinationType {
        QUEUE,
        TOPIC
    }

    public JMSHelper() {
        super();
    }

    public void cleanup() {
        connectionFactory = null;
        defaultDestination = null;
        defaultDestinationName = null;
        defaultDestinationType = null;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Destination getDefaultDestination() {
        return defaultDestination;
    }

    public void setDefaultDestination(Destination defaultDestination) {
        this.defaultDestination = defaultDestination;
    }

    public String getDefaultDestinationName() {
        return defaultDestinationName;
    }

    public void setDefaultDestinationName(String defaultDestinationName) {
        this.defaultDestinationName = defaultDestinationName;
    }

    public DestinationType getDefaultDestinationType() {
        return defaultDestinationType;
    }

    public void setDefaultDestinationType(DestinationType defaultDestinationType) {
        this.defaultDestinationType = defaultDestinationType;
    }

    public String sendTextMessage(Destination destination, final String payload, final Map<String, String> headerProperties) throws JMSException {
        return sendTextMessage(destination, payload, headerProperties, DeliveryMode.PERSISTENT, 4, null, null);
    }

    public String sendTextMessage(Destination destination, final String payload, final Map<String, String> headerProperties, Integer deliveryMode, Integer priority, String correlationID, Destination replyTo) throws JMSException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            if (null == connectionFactory)
                throw new JMSException("connection factory is null...can't do anything...");

            connection = connectionFactory.createConnection(); // Create connection
            boolean transacted = false;
            session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE); // Create Session

            //avoid another jndi lookup here
            Destination destinationLocal = null;
            if (null != destination) {
                destinationLocal = destination;
            } else {
                if (null != defaultDestination) {
                    destinationLocal = defaultDestination;
                } else {
                    if (!"".equals(defaultDestinationName)) {
                        if (null != defaultDestinationType) {
                            if (defaultDestinationType == DestinationType.QUEUE)
                                destinationLocal = session.createQueue(defaultDestinationName);
                            else if (defaultDestinationType == DestinationType.TOPIC)
                                destinationLocal = session.createTopic(defaultDestinationName);
                        } else {
                            throw new JMSException("destination type should be defined if using the destinationName construct");
                        }
                    } else {
                        throw new JMSException("destination name is null...can't do anything...");
                    }
                }
            }

            // Create Message Producer
            producer = session.createProducer(destinationLocal);
            if (null != deliveryMode)
                producer.setDeliveryMode(deliveryMode);

            if (null != priority)
                producer.setPriority(priority);

            // Create Message
            TextMessage msg = session.createTextMessage();
            msg.setText(payload);

            if (null != replyTo)
                msg.setJMSReplyTo(replyTo);

            if (null != correlationID)
                msg.setJMSCorrelationID(correlationID);

            if (null != headerProperties) {
                for (Entry<String, String> entry : headerProperties.entrySet()) {
                    msg.setStringProperty(entry.getKey(), entry.getValue());
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

    public String sendTextMessageAndWait(Destination destination, String textToSend, final Map<String, String> msgProperties, Integer deliveryMode, Integer priority, Destination replyTo, Integer replyWaitTimeoutMs) throws JMSException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        MessageConsumer responseConsumer = null;

        try {
            if (null == connectionFactory)
                throw new JMSException("connection factory is null...can't do anything...");

            connection = connectionFactory.createConnection(); // Create connection
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE); // Create Session

            //avoid another jndi lookup here
            Destination destinationLocal = null;
            if (null != destination) {
                destinationLocal = destination;
            } else {
                if (null != defaultDestination) {
                    destinationLocal = defaultDestination;
                } else {
                    if (!"".equals(defaultDestinationName)) {
                        if (null != defaultDestinationType) {
                            if (defaultDestinationType == DestinationType.QUEUE)
                                destinationLocal = session.createQueue(defaultDestinationName);
                            else if (defaultDestinationType == DestinationType.TOPIC)
                                destinationLocal = session.createTopic(defaultDestinationName);
                        } else {
                            throw new JMSException("destination type should be defined if using the destinationName construct");
                        }
                    } else {
                        throw new JMSException("destination name is null...can't do anything...");
                    }
                }
            }

            // Create Message Producer
            producer = session.createProducer(destinationLocal);
            if (null != deliveryMode)
                producer.setDeliveryMode(deliveryMode);

            if (null != priority)
                producer.setPriority(priority);

            // Create Message
            TextMessage msg = session.createTextMessage();
            msg.setText(textToSend);

            if (null != replyTo)
                msg.setJMSReplyTo(replyTo);

            //important: add the correlationID for sync correlation a bit later
            String correlationId = generateCorrelationID();
            msg.setJMSCorrelationID(correlationId);

            //add the replyTo destination
            msg.setJMSReplyTo(replyTo);

            if (null != msgProperties) {
                for (Entry<String, String> entry : msgProperties.entrySet()) {
                    msg.setStringProperty(entry.getKey(), entry.getValue());
                }
            }

            //create a consumer with a selector for the correlationid
            responseConsumer = session.createConsumer(replyTo, String.format("JMSCorrelationID='%s'", correlationId));

            //send the message
            producer.send(msg); // Send Message

            if (log.isDebugEnabled())
                log.debug("message sent successfully");

            //now wait for the response with a timeout (0 means infinite wait)
            if (replyWaitTimeoutMs <= 0) replyWaitTimeoutMs = 0;
            TextMessage receivedMessage = (TextMessage) responseConsumer.receive(replyWaitTimeoutMs);

            //if null, it means no message came within the timeout (or consumer got closed concurrently)
            if (null == receivedMessage) {
                if (log.isWarnEnabled())
                    log.warn("Didn't receive message response within timeout {} for Message CorrelationID {}", replyWaitTimeoutMs, msg.getJMSCorrelationID());
                return null;
            }

            return receivedMessage.getText();
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

    public static JMSHelper createTopicSender(Hashtable<String, String> jndiEnv, final String jndiConnectionFactory, String destinationName) {
        return createSender(jndiEnv, jndiConnectionFactory, destinationName, DestinationType.TOPIC);
    }

    public static JMSHelper createQueueSender(Hashtable<String, String> jndiEnv, final String jndiConnectionFactory, String destinationName) {
        return createSender(jndiEnv, jndiConnectionFactory, destinationName, DestinationType.QUEUE);
    }

    public static JMSHelper createSender(Hashtable<String, String> jndiEnv, final String jndiConnectionFactory, String destinationName, DestinationType destinationType) {
        if (null == jndiEnv) {
            throw new IllegalArgumentException("jndi params not defined.");
        } else if (log.isDebugEnabled()) {
            for (String key : jndiEnv.keySet()) {
                log.debug(String.format("Context: %s - %s", key.toString(), jndiEnv.get(key)));
            }
        }

        if (null == jndiConnectionFactory)
            throw new IllegalArgumentException("jms.connection.factory not defined.");

        // Lookup Connection Factory
        ConnectionFactory connectionFactory = null;
        try {
            Context namingContext = new InitialContext(jndiEnv);

            if (log.isDebugEnabled())
                log.debug("Context Created : " + namingContext.toString());

            connectionFactory = (ConnectionFactory) namingContext.lookup(jndiConnectionFactory);

            if (log.isDebugEnabled())
                log.debug("Lookup Connection Factory Success : " + connectionFactory.toString());
        } catch (NamingException e) {
            log.error("Issue with JNDI lookup", e);
        }

        if (null == jndiConnectionFactory)
            throw new IllegalArgumentException("JNDI Lookup failed: jms.connection.factory could not be found in the JNDI");

        return createSender(connectionFactory, destinationName, destinationType);
    }

    public static JMSHelper createSender(ConnectionFactory connectionFactory, String defaultDestinationName, DestinationType defaultDestinationType) {
        JMSHelper msgSender = new JMSHelper();
        msgSender.setConnectionFactory(connectionFactory);
        msgSender.setDefaultDestinationName(defaultDestinationName);
        msgSender.setDefaultDestinationType(defaultDestinationType);

        return msgSender;
    }

    public static JMSHelper createSender(ConnectionFactory connectionFactory) {
        JMSHelper msgSender = new JMSHelper();
        msgSender.setConnectionFactory(connectionFactory);
        return msgSender;
    }

    public static JMSHelper createSender(ConnectionFactory connectionFactory, Destination defaultDestination) {
        JMSHelper msgSender = new JMSHelper();
        msgSender.setConnectionFactory(connectionFactory);
        msgSender.setDefaultDestination(defaultDestination);

        return msgSender;
    }

    public static String generateCorrelationID() {
        return UUID.randomUUID().toString();
    }
}
