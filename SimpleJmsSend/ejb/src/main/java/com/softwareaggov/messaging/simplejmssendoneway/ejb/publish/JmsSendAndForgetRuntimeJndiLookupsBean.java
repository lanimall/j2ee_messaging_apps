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
 */com.softwareaggov.messaging.simplejmssendoneway.ejb.publish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/28/16.
 */

@Stateless(name = "JmsSendAndForgetRuntimeJndiLookupsService")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(JmsPublisherLocal.class)
@Remote(JmsPublisherRemote.class)
public class JmsSendAndForgetRuntimeJndiLookupsBean extends JmsPublisherBase implements JmsPublisherLocal {
    private static Logger log = LoggerFactory.getLogger(JmsSendAndForgetRuntimeJndiLookupsBean.class);

    @PostConstruct
    public void ejbCreate() {
        log.info("ejbCreate()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-create");
    }

    @Override
    public ConnectionFactory getJmsConnectionFactory() {
        return (ConnectionFactory) lookupEnvResource("jms/someManagedCF");
    }

    @Override
    public Destination getJmsDestination() {
        return (Destination) lookupEnvResource("jms/someManagedDestination");
    }

    private Object lookupEnvResource(String jndiLookupName) {
        Object resource = null;
        try {
            // create the context
            final Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            resource = envCtx.lookup(jndiLookupName);
        } catch (NamingException e) {
            log.warn("Could not lookup the resource " + jndiLookupName);
        }
        return resource;
    }

    @Override
    protected String sendMessage(Destination destination, boolean sessionTransacted, int sessionAcknowledgeMode, Object payload, Map<String, Object> headerProperties, Integer deliveryMode, Integer priority, String correlationID, Destination replyTo) throws JMSException {
        return null;
    }
}
