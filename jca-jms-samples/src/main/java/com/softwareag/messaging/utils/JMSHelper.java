package com.softwareag.messaging.utils;

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

    public static final String PAYLOAD_TEXTMSG_PROPERTY = "text";
    public static final String PAYLOAD_BYTES_PROPERTY = "padding";

    public enum DestinationType {
        QUEUE,
        TOPIC
    }

    public JMSHelper() {
        super();
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

    public void sendMessage(final Map<String,String> payload) throws JMSException {
        sendMessage(null, payload, null);
    }

    public void sendMessage(Destination destination, final Map<String,String> payload) throws JMSException {
        sendMessage(destination, payload, null);
    }

    public void sendMessage(Destination destination, final Map<String,String> payload, final Map<String,String> headerProperties) throws JMSException {
        sendMessage(destination, payload, headerProperties, null, null, DeliveryMode.PERSISTENT);
    }

    public void sendMessage(final Map<String,String> payload, final Map<String,String> headerProperties, String correlationID, Destination replyTo, int deliveryMode) throws JMSException {
        sendMessage(null, payload, headerProperties, correlationID, replyTo, deliveryMode);
    }

    public void sendMessage(Destination destination, final Map<String,String> payload, final Map<String,String> headerProperties, String correlationID, Destination replyTo, int deliveryMode) throws JMSException {
        Connection connection = null;

        try {
            if (null == connectionFactory)
                throw new JMSException("connection factory is null...can't do anything...");

            connection = connectionFactory.createConnection(); // Create connection
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE); // Create Session

            //avoid another jndi lookup here
            Destination destinationLocal = null;
            if(null != destination) {
                destinationLocal = destination;
            } else {
                if(null != defaultDestination){
                    destinationLocal = defaultDestination;
                } else {
                    if(!"".equals(defaultDestinationName)) {
                        if(null != defaultDestinationType) {
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
            MessageProducer producer = session.createProducer(destinationLocal);
            producer.setDeliveryMode(deliveryMode);

            // Create Message
            MapMessage msg = session.createMapMessage();
            if(null != payload){
                for (String payloadProperty : payload.keySet()){
                    msg.setStringProperty(payloadProperty, payload.get(payloadProperty));
                }
            }

            if(null != replyTo)
                msg.setJMSReplyTo(replyTo);

            if(null != correlationID)
                msg.setJMSCorrelationID(correlationID);

            if(null != headerProperties){
                for(Entry<String, String> entry : headerProperties.entrySet()){
                    msg.setStringProperty(entry.getKey(),entry.getValue());
                }
            }

            producer.send(msg); // Send Message

            if(log.isDebugEnabled())
                log.debug("message sent successfully");
        } catch (Exception e) {
            log.error("error while sending messages", e);
            throw new JMSException("Could not send messages");
        } finally {
            if (null != connection)
                connection.close();
        }
    }

    public String sendMessageAndWaitForReply(Destination destination, String textToSend, final Map<String,String> msgProperties, Destination replyTo) throws JMSException {
        Connection connection = null;

        try {
            if (null == connectionFactory)
                throw new JMSException("connection factory is null...can't do anything...");

            connection = connectionFactory.createConnection(); // Create connection
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE); // Create Session

            //avoid another jndi lookup here
            Destination destinationLocal = null;
            if(null != destination) {
                destinationLocal = destination;
            } else {
                if(null != defaultDestination){
                    destinationLocal = defaultDestination;
                } else {
                    if(!"".equals(defaultDestinationName)) {
                        if(null != defaultDestinationType) {
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
            MessageProducer producer = session.createProducer(destinationLocal);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            //create a new correlationid
            String correlationId = UUID.randomUUID().toString();

            //create a consumer with a selector for the correlationid
            MessageConsumer responseConsumer = session.createConsumer(replyTo, String.format("JMSCorrelationID='%s'", correlationId));

            // Create Message
            TextMessage msg = session.createTextMessage();

            //important: add the correlationID for sync correlation a bit later
            msg.setJMSCorrelationID(correlationId);

            //add the replyTo destination
            msg.setJMSReplyTo(replyTo);

            msg.setText(textToSend);
            if(null != msgProperties){
                for(Entry<String, String> entry : msgProperties.entrySet()){
                    msg.setStringProperty(entry.getKey(),entry.getValue());
                }
            }

            //send the message
            producer.send(msg); // Send Message

            if(log.isDebugEnabled())
                log.debug("message sent successfully");

            //now wait for the response with a timeout
            TextMessage receivedMessage = (TextMessage)responseConsumer.receive( 15000 );

            return receivedMessage.getText();
        } catch (Exception e) {
            log.error("error while sending/receiving messages", e);
            throw new JMSException("Could not send/receive messages");
        } finally {
            if (null != connection)
                connection.close();
        }
    }

    public static JMSHelper createTopicSender (String destinationName){
        return createSender(destinationName, DestinationType.TOPIC);
    }

    public static JMSHelper createQueueSender (String destinationName) {
        return createSender(destinationName, DestinationType.QUEUE);
    }

    public static JMSHelper createSender (String destinationName, DestinationType destinationType){
        String connectionUrl = AppConfig.getInstance().getPropertyHelper().getProperty("jms.connection.url");
        if (null == connectionUrl)
            throw new IllegalArgumentException("jms.connection.url not defined.");

        String jndiConnectionFactory = AppConfig.getInstance().getPropertyHelper().getProperty("jms.connection.factory");
        if (null == jndiConnectionFactory)
            throw new IllegalArgumentException("jms.connection.factory not defined.");

        String jndiContextFactory = AppConfig.getInstance().getPropertyHelper().getProperty("jms.jndi.contextfactory");
        if (null == jndiContextFactory)
            throw new IllegalArgumentException("jms.jndi.contextfactory not defined.");

        // creating properties file for getting initial context
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.factory.initial", jndiContextFactory);

        if (null != connectionUrl && !"".equals(connectionUrl)) {
            env.put("java.naming.provider.url", connectionUrl);
        }

        if (log.isDebugEnabled()) {
            for (String key : env.keySet()) {
                log.debug(String.format("Context: %s - %s", key.toString(), env.get(key)));
            }
        }

        if(log.isDebugEnabled())
            log.debug(String.format("System Properties: %s - %s", "java.naming.provider.url", System.getProperty("java.naming.provider.url", "not-set!")));

        // Lookup Connection Factory
        ConnectionFactory connectionFactory = null;
        try {
            Context namingContext = new InitialContext(env);

            if(log.isDebugEnabled())
                log.debug("Context Created : " + namingContext.toString());

            connectionFactory = (ConnectionFactory) namingContext.lookup(jndiConnectionFactory);

            if(log.isDebugEnabled())
                log.debug("Lookup Connection Factory Success : " + connectionFactory.toString());
        } catch (NamingException e) {
            log.error("Issue with JNDI lookup", e);
        }

        return createSender(connectionFactory, destinationName, destinationType);
    }

    public static JMSHelper createSender (ConnectionFactory connectionFactory, String defaultDestinationName, DestinationType defaultDestinationType){
        JMSHelper msgSender = new JMSHelper();
        msgSender.setConnectionFactory(connectionFactory);
        msgSender.setDefaultDestinationName(defaultDestinationName);
        msgSender.setDefaultDestinationType(defaultDestinationType);

        return msgSender;
    }

    public static JMSHelper createSender (ConnectionFactory connectionFactory) {
        JMSHelper msgSender = new JMSHelper();
        msgSender.setConnectionFactory(connectionFactory);
        return msgSender;
    }

    public static JMSHelper createSender (ConnectionFactory connectionFactory, Destination defaultDestination){
        JMSHelper msgSender = new JMSHelper();
        msgSender.setConnectionFactory(connectionFactory);
        msgSender.setDefaultDestination(defaultDestination);

        return msgSender;
    }

    public static String generateCorrelationID(){
        return UUID.randomUUID().toString();
    }
}
