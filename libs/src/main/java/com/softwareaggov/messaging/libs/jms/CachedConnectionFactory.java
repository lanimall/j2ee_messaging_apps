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
        if (null == internalConnectionFactory)
            throw new IllegalArgumentException("ConnectionFactory cannot be null");

        this.internalConnectionFactory = internalConnectionFactory;
    }

    private void checkAndCreateConnection() throws JMSException {
        checkAndCreateConnection(null, null);
    }

    private void checkAndCreateConnection(String userName, String password) throws JMSException {
        if (null == internalCachedConnection) {
            synchronized (padlock) {
                if (null == internalCachedConnection) {
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
        checkAndCreateConnection(userName, password);
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
