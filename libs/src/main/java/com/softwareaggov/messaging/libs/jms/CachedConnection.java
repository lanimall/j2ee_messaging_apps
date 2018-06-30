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

package com.softwareaggov.messaging.libs.jms;

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
