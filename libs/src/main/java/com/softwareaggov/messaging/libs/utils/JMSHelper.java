/*
 * Copyright © 2016 - 2018 Software AG, Darmstadt, Germany and/or its licensors
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.softwareaggov.messaging.libs.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.jms.IllegalStateException;
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

    static public enum JMSHeadersType {
        JMS_PAYLOAD,
        JMS_MESSAGEID,
        JMS_CORRELATIONID,
        JMS_DELIVERYMODE,
        JMS_PRIORITY,
        JMS_DESTINATION,
        JMS_TIMESTAMP,
        JMS_EXPIRATION,
        JMS_TYPE,
        JMS_REPLYTO,
        JMS_REDELIVERED;
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

    public String sendTextMessage(final Object payload, Map<JMSHeadersType, Object> jmsProperties, final Map<String, Object> customProperties) throws JMSException {
        return sendTextMessage(payload, jmsProperties, customProperties, false, Session.AUTO_ACKNOWLEDGE);
    }

    public String sendTextMessage(final Object payload, final Map<JMSHeadersType, Object> jmsProperties, final Map<String, Object> customProperties, boolean sessionTransacted, int sessionAcknowledgeMode) throws JMSException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            if (null == connectionFactory) {
                throw new JMSException("connection factory is null...can't do anything...");
            }

            if (sessionAcknowledgeMode != Session.AUTO_ACKNOWLEDGE && sessionAcknowledgeMode != Session.CLIENT_ACKNOWLEDGE && sessionAcknowledgeMode != Session.DUPS_OK_ACKNOWLEDGE) {
                throw new JMSException("invalid ack mode...can't do anything...");
            }

            if (log.isTraceEnabled()) {
                String msgJMSHeaderPropertiesStr = JMSHelper.getMessageJMSHeaderPropsAsString(jmsProperties, ",");
                String msgCustomPropertiesStr = JMSHelper.getMessagePropertiesAsString(customProperties, ",");

                log.trace("Message data to send: {}, \nJMS Headers: {}, \nCustom Properties: {}",
                        ((null != payload) ? payload : "null"),
                        ((null != msgJMSHeaderPropertiesStr) ? msgJMSHeaderPropertiesStr : "null"),
                        ((null != msgCustomPropertiesStr) ? msgCustomPropertiesStr : "null"));
            }

            //create connection and session
            connection = connectionFactory.createConnection(); // Create connection
            session = connection.createSession(sessionTransacted, sessionAcknowledgeMode); // Create Session

            if (null == jmsProperties.get(JMSHeadersType.JMS_DESTINATION))
                throw new JMSException("Destination is null...can't do anything...");

            // Create Message Producer
            producer = session.createProducer((Destination) jmsProperties.get(JMSHeadersType.JMS_DESTINATION));

            if (null != jmsProperties.get(JMSHeadersType.JMS_DELIVERYMODE)) {
                int jmsDeliveryMode = (Integer) jmsProperties.get(JMSHeadersType.JMS_DELIVERYMODE);
                if (jmsDeliveryMode == DeliveryMode.NON_PERSISTENT || jmsDeliveryMode == DeliveryMode.PERSISTENT)
                    producer.setDeliveryMode(jmsDeliveryMode);
            }

            if (null != jmsProperties.get(JMSHeadersType.JMS_PRIORITY)) {
                int jmsPriority = (Integer) jmsProperties.get(JMSHeadersType.JMS_PRIORITY);
                if (jmsPriority >= 0)
                    producer.setPriority(jmsPriority);
            }

            // Create Message
            TextMessage msg = session.createTextMessage();
            if (null != payload)
                msg.setText((String) payload); // TODO: temporary...here we need to support more types!!!!

            if (null != jmsProperties.get(JMSHeadersType.JMS_REPLYTO))
                msg.setJMSReplyTo((Destination) jmsProperties.get(JMSHeadersType.JMS_REPLYTO));

            if (null != jmsProperties.get(JMSHeadersType.JMS_CORRELATIONID))
                msg.setJMSCorrelationID((String) jmsProperties.get(JMSHeadersType.JMS_CORRELATIONID));

            //add the custom properties
            if (null != customProperties) {
                for (Entry<String, Object> entry : customProperties.entrySet()) {
                    msg.setObjectProperty(entry.getKey(), entry.getValue());
                }
            }

            producer.send(msg); // Send Message

            if (sessionTransacted) {
                if (log.isDebugEnabled())
                    log.debug("About to commit Session (send)");
                session.commit();
            }

            if (log.isDebugEnabled())
                log.debug("message sent successfully");

            return msg.getJMSMessageID();
        } catch (JMSException je) {
            if (log.isDebugEnabled())
                log.error("JMSException on send.", je);

            if (sessionTransacted) {
                try {
                    if (log.isDebugEnabled())
                        log.error("Since transaction is enabled, doing rollback of session due to JMSException.", je);

                    if (null != session) {
                        session.rollback();
                        if (log.isDebugEnabled())
                            log.debug("Rolled back session.");
                    }
                } catch (Throwable t) {
                    if (log.isDebugEnabled())
                        log.error("Session rollback failed with exception", t);
                }
            }

            throw je;
        } finally {
            if (null != producer)
                producer.close();

            if (null != session)
                session.close();

            if (null != connection)
                connection.close();
        }
    }

    public String sendTextMessageAndWait(final Object payload, final Map<JMSHeadersType, Object> jmsProperties, final Map<String, Object> customProperties, Long replyWaitTimeoutMs) throws JMSException {
        return sendTextMessageAndWait(payload, jmsProperties, customProperties, replyWaitTimeoutMs, false, Session.AUTO_ACKNOWLEDGE);
    }

    public String sendTextMessageAndWait(final Object payload, final Map<JMSHeadersType, Object> jmsProperties, final Map<String, Object> customProperties, Long replyWaitTimeoutMs, final boolean sessionTransacted, final int sessionAcknowledgeMode) throws JMSException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        MessageConsumer responseConsumer = null;

        try {
            if (null == connectionFactory)
                throw new JMSException("connection factory is null...can't do anything...");

            if (sessionAcknowledgeMode != Session.AUTO_ACKNOWLEDGE && sessionAcknowledgeMode != Session.CLIENT_ACKNOWLEDGE && sessionAcknowledgeMode != Session.DUPS_OK_ACKNOWLEDGE) {
                throw new JMSException("invalid ack mode...can't do anything...");
            }

            if (log.isTraceEnabled()) {
                String msgJMSHeaderPropertiesStr = JMSHelper.getMessageJMSHeaderPropsAsString(jmsProperties, ",");
                String msgCustomPropertiesStr = JMSHelper.getMessagePropertiesAsString(customProperties, ",");

                log.trace("Message data to send: {}, \nJMS Headers: {}, \nCustom Properties: {}",
                        ((null != payload) ? payload : "null"),
                        ((null != msgJMSHeaderPropertiesStr) ? msgJMSHeaderPropertiesStr : "null"),
                        ((null != msgCustomPropertiesStr) ? msgCustomPropertiesStr : "null"));
            }

            //create connection and session
            connection = connectionFactory.createConnection(); // Create connection
            session = connection.createSession(sessionTransacted, sessionAcknowledgeMode); // Create Session

            Object targetDestination = jmsProperties.get(JMSHeadersType.JMS_DESTINATION);
            if (null == targetDestination)
                throw new JMSException("Destination is null...can't do anything...");

            //here, we could fall back on creating a temp response queue if reply is not specified...likely a good thing to try
            Object replyToDestination = jmsProperties.get(JMSHeadersType.JMS_REPLYTO);
            if (null == replyToDestination)
                throw new JMSException("ReplyTo Destination is null...can't do anything...");

            // Create Message Producer
            producer = session.createProducer((Destination) targetDestination);

            if (null != jmsProperties.get(JMSHeadersType.JMS_DELIVERYMODE)) {
                int jmsDeliveryMode = (Integer) jmsProperties.get(JMSHeadersType.JMS_DELIVERYMODE);
                if (jmsDeliveryMode == DeliveryMode.NON_PERSISTENT || jmsDeliveryMode == DeliveryMode.PERSISTENT)
                    producer.setDeliveryMode(jmsDeliveryMode);
            }

            if (null != jmsProperties.get(JMSHeadersType.JMS_PRIORITY)) {
                int jmsPriority = (Integer) jmsProperties.get(JMSHeadersType.JMS_PRIORITY);
                if (jmsPriority >= 0)
                    producer.setPriority(jmsPriority);
            }

            // Create Message
            TextMessage msg = session.createTextMessage();
            if (null != payload)
                msg.setText((String) payload); // TODO: temporary...here we need to support more types!!!!

            if (null != replyToDestination)
                msg.setJMSReplyTo((Destination) replyToDestination);

            if (null != customProperties) {
                for (Entry<String, Object> entry : customProperties.entrySet()) {
                    msg.setObjectProperty(entry.getKey(), entry.getValue());
                }
            }

            //send the message
            producer.send(msg); // Send Message

            if (sessionTransacted) {
                if (log.isDebugEnabled())
                    log.debug("About to commit Session (send)");
                session.commit();
            }

            if (log.isDebugEnabled())
                log.debug("message sent successfully");

            /// wait for REPLY section..

            // Create a selector to only get the reply message that matches my request message id.
            String selector = String.format("JMSCorrelationID='%s'", msg.getJMSMessageID());

            //create a consumer with the selector
            responseConsumer = session.createConsumer((Destination) replyToDestination, selector);

            // Start the connection
            connection.start();

            //now wait for the response with a timeout (if null or <0, wait infinite -- 0 means infinite wait)
            if (null == replyWaitTimeoutMs || replyWaitTimeoutMs <= 0) replyWaitTimeoutMs = 0L;
            Message receivedMessage = responseConsumer.receive(replyWaitTimeoutMs);

            if (null == receivedMessage) {
                throw new JMSException(String.format("Didn't receive message response within timeout [%d] for Message ID [%s]", replyWaitTimeoutMs, msg.getJMSMessageID()));
            }

            if (sessionTransacted) {
                if (log.isDebugEnabled())
                    log.debug("About to commit Session (receive)");
                session.commit();
            }

            //if null, it means no message came within the timeout (or consumer got closed concurrently)
            if (null != receivedMessage && receivedMessage instanceof TextMessage) {
                return ((TextMessage) receivedMessage).getText();
            } else {
                throw new IllegalStateException(String.format("Message response type not expected - received: [%s] / expected: [%s]", receivedMessage.getClass().getName(), TextMessage.class.getName()));
            }
        } catch (JMSException je) {
            if (log.isDebugEnabled())
                log.error("JMSException on send.", je);

            if (sessionTransacted) {
                try {
                    if (log.isDebugEnabled())
                        log.error("Since transaction is enabled, doing rollback of session due to JMSException.", je);

                    if (null != session) {
                        session.rollback();
                        if (log.isDebugEnabled())
                            log.debug("Rolled back session.");
                    }
                } catch (Throwable t) {
                    if (log.isDebugEnabled())
                        log.error("Session rollback failed with exception", t);
                }
            }

            throw je;
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

    public static String getMessagePropertiesAsString(Message msg, String delimiter) throws JMSException {
        return getMessagePropertiesAsString(JMSHelper.getMessageProperties(msg), delimiter);
    }

    public static String getMessagePropertiesAsString(Map<String, Object> messageProperties, String delimiter) throws JMSException {
        String messagePropertiesStr = "";

        //transform the property map into a string
        if (null != messageProperties) {
            for (Map.Entry<String, Object> header : messageProperties.entrySet()) {
                messagePropertiesStr += String.format("[%s%s%s]", header.getKey(), (null != delimiter) ? delimiter : ",", (null != header.getValue()) ? header.getValue().toString() : "null");
            }
        }

        return messagePropertiesStr;
    }

    public static Map<JMSHeadersType, Object> getMessageJMSHeaderPropsAsMap(Message msg) throws JMSException {
        Map<JMSHeadersType, Object> jmsHeaderProps = null;
        if (null != msg) {
            jmsHeaderProps = new HashMap<JMSHeadersType, Object>();

            if (null != msg.getJMSMessageID() && !"".equals(msg.getJMSMessageID()))
                jmsHeaderProps.put(JMSHeadersType.JMS_MESSAGEID, msg.getJMSMessageID());

            if (null != msg.getJMSCorrelationID() && !"".equals(msg.getJMSCorrelationID()))
                jmsHeaderProps.put(JMSHeadersType.JMS_CORRELATIONID, msg.getJMSCorrelationID());

            if (null != msg.getJMSDestination())
                jmsHeaderProps.put(JMSHeadersType.JMS_DESTINATION, msg.getJMSDestination());

            if (null != msg.getJMSReplyTo())
                jmsHeaderProps.put(JMSHeadersType.JMS_REPLYTO, msg.getJMSReplyTo());

            if (null != msg.getJMSType() && !"".equals(msg.getJMSType()))
                jmsHeaderProps.put(JMSHeadersType.JMS_TYPE, msg.getJMSType());

            jmsHeaderProps.put(JMSHeadersType.JMS_REDELIVERED, msg.getJMSRedelivered());
            jmsHeaderProps.put(JMSHeadersType.JMS_TIMESTAMP, msg.getJMSTimestamp());
            jmsHeaderProps.put(JMSHeadersType.JMS_EXPIRATION, msg.getJMSExpiration());
            jmsHeaderProps.put(JMSHeadersType.JMS_DELIVERYMODE, msg.getJMSDeliveryMode());
            jmsHeaderProps.put(JMSHeadersType.JMS_PRIORITY, msg.getJMSPriority());
        }

        return jmsHeaderProps;
    }

    public static Map<JMSHeadersType, Object> getMessageJMSHeaderPropsAsMap(final Destination destination, final Integer deliveryMode, final Integer priority, final String correlationID, final Destination replyTo) throws JMSException {
        Map<JMSHeadersType, Object> jmsHeaderProps = new HashMap<JMSHeadersType, Object>();
        jmsHeaderProps.put(JMSHeadersType.JMS_CORRELATIONID, correlationID);
        jmsHeaderProps.put(JMSHeadersType.JMS_DELIVERYMODE, deliveryMode);
        jmsHeaderProps.put(JMSHeadersType.JMS_PRIORITY, priority);
        jmsHeaderProps.put(JMSHeadersType.JMS_DESTINATION, destination);
        jmsHeaderProps.put(JMSHeadersType.JMS_REPLYTO, replyTo);

        return jmsHeaderProps;
    }

    public static String getMessageJMSHeaderPropsAsString(Message msg, String delimiter) throws JMSException {
        return getMessageJMSHeaderPropsAsString(JMSHelper.getMessageJMSHeaderPropsAsMap(msg), delimiter);
    }

    public static String getMessageJMSHeaderPropsAsString(Map<JMSHeadersType, Object> messageJMSHeaderProps, String delimiter) throws JMSException {
        String messageJMSHeaderPropsStr = "";
        //transform the property map into a string
        if (null != messageJMSHeaderProps) {
            for (Map.Entry<JMSHeadersType, Object> header : messageJMSHeaderProps.entrySet()) {
                messageJMSHeaderPropsStr += String.format("[%s%s%s]", header.getKey(), (null != delimiter) ? delimiter : ",", (null != header.getValue()) ? header.getValue().toString() : "null");
            }
        }

        return messageJMSHeaderPropsStr;
    }
}
