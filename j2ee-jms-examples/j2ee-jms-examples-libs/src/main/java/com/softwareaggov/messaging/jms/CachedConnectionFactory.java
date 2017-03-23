package com.softwareaggov.messaging.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * Created by fabien.sanglier on 2/27/17.
 */
public class CachedConnectionFactory implements ConnectionFactory {
    private final ConnectionFactory internalConnectionFactory;
    private Connection internalCachedConnection;

    private volatile Object padlock = new Object();

    public CachedConnectionFactory(ConnectionFactory internalConnectionFactory) {
        if(null == internalConnectionFactory)
            throw new IllegalArgumentException("ConnectionFactory cannot be null");

        this.internalConnectionFactory = internalConnectionFactory;
    }

    private void checkAndCreateConnection() throws JMSException {
        checkAndCreateConnection(null, null);
    }

    private void checkAndCreateConnection(String userName, String password) throws JMSException {
        if (null == internalCachedConnection) {
            synchronized (padlock) {
                if(null == internalCachedConnection) {
                    if (null != userName || null != password)
                        internalCachedConnection = internalConnectionFactory.createConnection(userName, password);
                    else
                        internalCachedConnection = internalConnectionFactory.createConnection();
                }
            }
        }
    }

    @Override
    public Connection createConnection() throws JMSException {
        checkAndCreateConnection();
        return new CachedConnection(internalCachedConnection);
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        checkAndCreateConnection(userName,password);
        return new CachedConnection(internalCachedConnection);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if (null != internalCachedConnection)
            internalCachedConnection.close();
        internalCachedConnection = null;
    }
}
