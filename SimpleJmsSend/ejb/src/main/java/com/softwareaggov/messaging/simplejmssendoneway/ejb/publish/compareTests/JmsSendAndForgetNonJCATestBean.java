/*
 *
 *
 *  Copyright © 2016 - 2018 Software AG, Darmstadt, Germany and/or its licensors
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * /
 */jb.publish.compareTests;

import com.softwareaggov.messaging.libs.utils.JMSHelper;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsPublisher;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsPublisherLocal;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.utils.CounterLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by fabien.sanglier on 10/12/17.
 */
@Stateless(name = "JmsSendAndForgetNonJCATestService")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(JmsPublisherLocal.class)
public class JmsSendAndForgetNonJCATestBean implements JmsPublisher {
    private static Logger log = LoggerFactory.getLogger(JmsSendAndForgetNonJCATestBean.class);

    @EJB
    private CounterLocal messageProcessingCounter;

    @Resource(name = "jmsSendEnabled")
    private Boolean isEnabled = true;

    @Resource(name = "jms.jndi.contextfactory")
    private String jndiContextFactory = null;

    @Resource(name = "jms.jndi.connection.url")
    private String jndiConnectionUrl = null;

    @Resource(name = "jms.connectionfactory.name")
    private String jmsConnectionFactoryName = null;

    @Resource(name = "jms.default.destination.name")
    private String jmsDefaultDestinationName = null;

    @Resource(name = "jmsDeliveryMode")
    private Integer jmsDeliveryMode = null;

    @Resource(name = "jmsPriority")
    private Integer jmsPriority = null;

    @Resource(name = "jmsReplyDestinationName")
    private String jmsReplyDestinationName = null;

    @Resource(name = "jmsReplyDestinationType")
    private String jmsReplyDestinationType = null;

    protected transient JMSHelper jmsHelper;
    private transient Destination jmsReplyTo;

    private ConnectionFactory connectionFactory;
    private Destination defaultDestination;
    private volatile boolean init = false;

    public void ejbCreate() {
        log.info("ejbCreate()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-create");
    }

    @PreDestroy
    private void cleanup() {
        log.info("ejbRemove()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-remove");
        jmsReplyTo = null;
        connectionFactory = null;
        defaultDestination = null;

        if (null != jmsHelper)
            jmsHelper.cleanup();
        jmsHelper = null;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    protected String getBeanName() {
        return this.getClass().getSimpleName();
    }

    protected void initJMS() throws JMSException, NamingException {
        if (!init) {
            synchronized (this.getClass()) {
                if (!init) {
                    //JNDI params
                    if (null == jndiContextFactory)
                        throw new IllegalArgumentException("jms.jndi.contextfactory not defined.");

                    if (null == jndiConnectionUrl)
                        throw new IllegalArgumentException("jms.jndi.connection.url not defined.");

                    Hashtable<String, String> jndiEnv = new Hashtable<String, String>();
                    jndiEnv.put("java.naming.factory.initial", jndiContextFactory);

                    if (null != jndiConnectionUrl && !"".equals(jndiConnectionUrl)) {
                        //add the proptocol if not set -- default to nsp
                        if (-1 == jndiConnectionUrl.indexOf("://"))
                            jndiConnectionUrl = "nsp://" + jndiConnectionUrl;

                        jndiEnv.put("java.naming.provider.url", jndiConnectionUrl);
                    }

                    //JMS connection factory
                    if (null == jmsConnectionFactoryName)
                        throw new IllegalArgumentException("jms.connectionfactory.name not defined.");

                    connectionFactory = (ConnectionFactory) JMSHelper.lookupJNDI(jndiEnv, jmsConnectionFactoryName);

                    //set the default destination only if it's not set already
                    if (null != jmsDefaultDestinationName) {
                        defaultDestination = (Destination) JMSHelper.lookupJNDI(jndiEnv, jmsDefaultDestinationName);
                    }

                    this.jmsHelper = JMSHelper.createSender(connectionFactory);

                    //JMS reply destination
                    jmsReplyTo = null;
                    if (null != jmsReplyDestinationName && !"".equals(jmsReplyDestinationName) &&
                            null != jmsReplyDestinationType && !"".equals(jmsReplyDestinationType)) {
                        jmsReplyTo = jmsHelper.lookupDestination(jmsReplyDestinationName, jmsReplyDestinationType);
                    }

                    init = true;
                }
            }
        }
    }

    protected String sendMessage(Destination destination, final Object payload, final Map<String, Object> headerProperties, Integer deliveryMode, Integer priority, String correlationID, Destination replyTo) throws JMSException {
        Map<JMSHelper.JMSHeadersType, Object> jmsProperties = JMSHelper.getMessageJMSHeaderPropsAsMap(destination, deliveryMode, priority, correlationID, replyTo);
        return jmsHelper.sendTextMessage(payload, jmsProperties, headerProperties);
    }

    @TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
    public String sendTextMessage(final Object msgTextPayload, final Map<String, Object> msgHeaderProperties) {
        String returnText = "";
        if (log.isDebugEnabled())
            log.debug("in EJB: sendTextMessage");

        try {
            initJMS();

            returnText = sendMessage(defaultDestination, msgTextPayload, msgHeaderProperties, jmsDeliveryMode, jmsPriority, null, jmsReplyTo);

            //increment processing counter
            messageProcessingCounter.incrementAndGet(getBeanName());

            if (null == returnText) {
                messageProcessingCounter.incrementAndGet(getBeanName() + "-responseIsNull");
            } else {
                messageProcessingCounter.incrementAndGet(getBeanName() + "-responseNotNull");
            }
        } catch (Exception e) {
            messageProcessingCounter.incrementAndGet(getBeanName() + "-errors");
            log.error("JMS Error occurred", e);
            throw new EJBException(e);
        }

        return returnText;
    }
}
