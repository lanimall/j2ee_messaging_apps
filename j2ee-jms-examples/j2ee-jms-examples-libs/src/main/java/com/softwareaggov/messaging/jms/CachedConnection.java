package com.softwareaggov.messaging.jms;

import javax.jms.*;

/**
 * Created by fabien.sanglier on 2/27/17.
 */
public class CachedConnection implements Connection {
    private final Connection internalConnection;

    public CachedConnection(Connection internalConnection) {
        this.internalConnection = internalConnection;
    }

    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        return internalConnection.createSession(transacted, acknowledgeMode);
    }

    @Override
    public String getClientID() throws JMSException {
        return internalConnection.getClientID();
    }

    @Override
    public void setClientID(String clientID) throws JMSException {
        internalConnection.setClientID(clientID);
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        return internalConnection.getMetaData();
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        return internalConnection.getExceptionListener();
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) throws JMSException {
        internalConnection.setExceptionListener(listener);
    }

    @Override
    public void start() throws JMSException {
        internalConnection.start();
    }

    @Override
    public void stop() throws JMSException {
        internalConnection.stop();
    }

    @Override
    public void close() throws JMSException {
        //do nothing here...
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return internalConnection.createConnectionConsumer(destination, messageSelector, sessionPool, maxMessages);
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        return internalConnection.createDurableConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
    }
}
