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

package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.compareTests;

import com.softwareaggov.messaging.libs.jms.CachedConnectionFactory;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsPublisherLocal;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsSendAndForgetBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

/**
 * Created by fabien.sanglier on 6/28/16.
 */

@Stateless(name = "JmsSendAndForgetCachedConnectionTestService")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(JmsPublisherLocal.class)
public class JmsSendAndForgetCachedConnectionTestBean extends JmsSendAndForgetBean {
    private static Logger log = LoggerFactory.getLogger(JmsSendAndForgetCachedConnectionTestBean.class);

    @Resource(name = "jms/someManagedCF")
    private ConnectionFactory jmsConnectionFactory;

    @Resource(name = "jms/someManagedDestination")
    private Destination jmsDestination;

    @Override
    public ConnectionFactory getJmsConnectionFactory() {
        return new CachedConnectionFactory(jmsConnectionFactory);
    }

    @Override
    public Destination getJmsDestination() {
        return jmsDestination;
    }
}