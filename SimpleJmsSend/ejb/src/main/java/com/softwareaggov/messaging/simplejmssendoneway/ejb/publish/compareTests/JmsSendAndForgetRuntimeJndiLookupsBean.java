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

import com.softwareaggov.messaging.libs.interop.MessageInterop;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsPublisherBase;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.MessageInteropLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/28/16.
 */

@Stateless(name = "JmsSendAndForgetRuntimeJndiLookupsService")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(MessageInteropLocal.class)
@Remote(MessageInterop.class)
public class JmsSendAndForgetRuntimeJndiLookupsBean extends JmsPublisherBase {
    private static Logger log = LoggerFactory.getLogger(JmsSendAndForgetRuntimeJndiLookupsBean.class);

    @Override
    protected ConnectionFactory getJmsConnectionFactory() {
        return (ConnectionFactory) lookupEnvResource(JmsPublisherBase.RESOURCE_NAME_CF);
    }

    @Override
    protected Destination getJmsDestination() {
        return (Destination) lookupEnvResource(JmsPublisherBase.RESOURCE_NAME_DEST);
    }

    @Override
    protected Destination getJmsReplyToDestination() {
        return (Destination) lookupEnvResource(JmsPublisherBase.RESOURCE_NAME_REPLYDEST);
    }

    @Override
    protected String sendMessage(ConnectionFactory jmsConnectionFactory, Destination destination, boolean sessionTransacted, int sessionAcknowledgeMode, Object payload, Map<String, Object> headerProperties, Integer deliveryMode, Integer priority, String correlationID, Destination replyTo) throws JMSException {
        return null;
    }
}
