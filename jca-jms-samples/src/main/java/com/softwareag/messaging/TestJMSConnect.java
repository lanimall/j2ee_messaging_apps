package com.softwareag.messaging;

import com.softwareag.messaging.utils.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;

/**
 * Created by FabienSanglier on 3/14/15.
 */
public class TestJMSConnect {
    private static Logger log = LoggerFactory.getLogger(TestJMSConnect.class);

    private static final long serialVersionUID = 1L;

    public TestJMSConnect() {
        super();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Testing");

        try {
            String connectionUrl = AppConfig.getInstance().getPropertyHelper().getProperty("jms.connection.url");
            if (null == connectionUrl)
                throw new RuntimeException("jms.connection.url not defined.");

            String jndiConnectionFactory = AppConfig.getInstance().getPropertyHelper().getProperty("jms.connection.factory");
            if (null == jndiConnectionFactory)
                throw new RuntimeException("jms.connection.factory not defined.");

            boolean isQueue = true;
            String destinationType = AppConfig.getInstance().getPropertyHelper().getProperty("jms.destination.type", "queue");
            if ("topic".equalsIgnoreCase(destinationType)) {
                isQueue = false;
            } else if ("queue".equalsIgnoreCase(destinationType)) {
                isQueue = true;
            } else {
                throw new RuntimeException("jms.destination.type not valid.");
            }

            String destinationName = AppConfig.getInstance().getPropertyHelper().getProperty("jms.destination.name");
            if (null == destinationName)
                throw new RuntimeException("jms.destination.name not defined.");

            int msgTotal = AppConfig.getInstance().getPropertyHelper().getPropertyAsInt("default.message.count", 10);

            log.debug(String.format("Sending %d messages to url [%s], %s name [%s], using factory jndi name [%s]", msgTotal, connectionUrl, (isQueue) ? "queue" : "topic", destinationName, jndiConnectionFactory));

            int msgCount = 0;
            for (msgCount = 0; msgCount < msgTotal; msgCount++) {
                String message = "This is message " + (msgCount + 1);
                sendMessage(connectionUrl, jndiConnectionFactory, destinationName, message, isQueue);
            }

            System.out.println(msgCount + " messages sent successfully");
        } catch (JMSException e) {
            log.error("Error occurred", e);
        }
    }

    private static void sendMessage(String connectionUrl, String jndiConnectionFactory, String destinationName, String textToSend, boolean isQueue) throws JMSException {
        Connection connection = null;

        try
        {
            String contextFactory = AppConfig.getInstance().getPropertyHelper().getProperty("jms.connection.contextfactory");
            if (null == contextFactory)
                throw new RuntimeException("jms.connection.contextfactory not defined.");

            // creating properties file for getting initial context
            Hashtable<String,String> env = new Hashtable<String,String>();
            env.put("java.naming.factory.initial", contextFactory);

            if(null != connectionUrl && !"".equals(connectionUrl)){
                env.put("java.naming.provider.url", connectionUrl);
            }

            for(String key : env.keySet()){
                log.debug(String.format("Context: %s - %s",key.toString(), env.get(key)));
            }

            env.put("java.naming.security.authentication", "simple");

            log.info(String.format("System Properties: %s - %s","java.naming.provider.url", System.getProperty("java.naming.provider.url", "not-set!")));

            Context namingContext = new InitialContext(env);

            log.info("Context Created : " + namingContext.toString());

            // Lookup Connection Factory
            ConnectionFactory connectionFactory = (ConnectionFactory) namingContext.lookup(jndiConnectionFactory);
            log.info("Lookup Connection Factory Success : " + connectionFactory.toString());

            connection = connectionFactory.createConnection(); // Create connection

            log.info("Connection Created : " + connection.toString());

            Session session = null;
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE); // Create Session

            log.info("Session Created : " + connection.toString());

            //avoid another jndi lookup here
            Destination destination = (Destination)namingContext.lookup(destinationName);

            log.info("Destination Created : " + destination);

            // Create Message Producer
            MessageProducer producer = session.createProducer(destination);

            // Create Message
            TextMessage msg = session.createTextMessage();
            msg.setText(textToSend);

            log.info(String.format("Sending new message to %s %s : %s ", (isQueue) ? "queue" : "topic", destinationName, textToSend));

            producer.send(msg); // Send Message

            log.info("message sent successfully");
        }
        catch (Exception e)
        {
            log.error("error while sending messages", e);
            throw new JMSException("Could not send messages");
        } finally {
            if(null != connection)
                connection.close();
        }
    }
}

