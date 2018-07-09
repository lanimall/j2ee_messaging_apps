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

package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish;

import com.softwareaggov.messaging.libs.utils.Counter;
import com.softwareaggov.messaging.libs.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;

/*
 * Simple JMS Publisher bean relying on the connection factory to create the JMS connection on each call
 * Created by fabien.sanglier on 6/15/16.
 */
public abstract class JmsPublisherBase implements JmsPublisher {
    private static Logger log = LoggerFactory.getLogger(JmsPublisherBase.class);

    public static final String RESOURCE_NAME_CF = "jms/someManagedCF";
    public static final String RESOURCE_NAME_DEST = "jms/someManagedDestination";
    public static final String RESOURCE_NAME_REPLYDEST = "jms/someManagedReplyToDestination";

    @EJB(beanName = "CounterService")
    protected Counter messageProcessingCounter;

    @Resource(name = "jmsSessionTransacted")
    private Boolean jmsSessionTransacted = Boolean.FALSE;

    @Resource(name = "jmsSessionAcknowledgeMode")
    private Integer jmsSessionAcknowledgeMode = Session.AUTO_ACKNOWLEDGE;

    @Resource(name = "jmsDeliveryMode")
    private Integer jmsDeliveryMode = null;

    @Resource(name = "jmsPriority")
    private Integer jmsPriority = null;

//    @Resource(name = "jmsReplyDestinationName")
//    private String jmsReplyDestinationName = null;
//
//    @Resource(name = "jmsReplyDestinationType")
//    private String jmsReplyDestinationType = null;

    @Resource(name = "jmsSendEnabled")
    private Boolean isEnabled;

//    private volatile boolean init = false;

//    protected transient JMSHelper jmsHelper = null;

    @PostConstruct
    public void ejbCreate() {
        log.info("ejbCreate()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-create");
    }

    @PreDestroy
    public void ejbRemove() throws EJBException {
        log.info("ejbRemove()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-remove");
//        init = false;
//        if (null != jmsHelper)
//            jmsHelper.cleanup();
//        jmsHelper = null;
    }

    protected String getBeanName() {
        return this.getClass().getSimpleName();
    }

    protected abstract ConnectionFactory getJmsConnectionFactory();

    protected abstract Destination getJmsDestination();

    protected abstract Destination getJmsReplyToDestination();

    protected abstract String sendMessage(ConnectionFactory jmsConnectionFactory, Destination destination, boolean sessionTransacted, int sessionAcknowledgeMode, final Object payload, final Map<String, Object> headerProperties, Integer deliveryMode, Integer priority, String correlationID, Destination replyTo) throws JMSException;

    protected Object lookupEnvResource(String jndiLookupName) {
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

    public boolean isEnabled() {
        return (null != isEnabled) ? isEnabled : false;
    }

    // Initializing resource outside the EJB creation to make sure these lookups get retried until they work
    // (eg. if UM is not available yet when EJB is created)
//    private void initJMS() throws JMSException {
//        if (!init) {
//            synchronized (this.getClass()) {
//                if (!init) {
//                    try {
//                        this.jmsHelper = JMSHelper.createSender(getJmsConnectionFactory());
//
//                        //JMS reply destination
//                        if (null != jmsReplyDestinationName && !"".equals(jmsReplyDestinationName) &&
//                                null != jmsReplyDestinationType && !"".equals(jmsReplyDestinationType)) {
//                            jmsReplyTo = jmsHelper.lookupDestination(jmsReplyDestinationName, jmsReplyDestinationType);
//                        }
//
//                        init = true;
//                        messageProcessingCounter.incrementAndGet(getBeanName() + "-initSuccess");
//                    } catch (JMSException e) {
//                        messageProcessingCounter.incrementAndGet(getBeanName() + "-initErrors");
//                        throw e;
//                    }
//                }
//            }
//        }
//    }

    public String sendTextMessage(final Object msgTextPayload, final Map<String, Object> msgHeaderProperties) throws JMSException {
        String returnText = "";
        if (log.isDebugEnabled())
            log.debug("in EJB: sendTextMessage");

        try {
            //Initialize JMS objects...once done, will not do it again
//            initJMS();

            returnText = sendMessage(getJmsConnectionFactory(), getJmsDestination(), jmsSessionTransacted, jmsSessionAcknowledgeMode, msgTextPayload, msgHeaderProperties, jmsDeliveryMode, jmsPriority, null, getJmsReplyToDestination());

            //increment processing counter
            messageProcessingCounter.incrementAndGet(getBeanName() + "-messageSent");

            if (null == returnText) {
                messageProcessingCounter.incrementAndGet(getBeanName() + "-responseIsNull");
            } else {
                messageProcessingCounter.incrementAndGet(getBeanName() + "-responseNotNull");
            }
        } catch (Exception e) {
            log.error("Exception occurred", e);
            messageProcessingCounter.incrementAndGet(getBeanName() + "-errors");
            throw new EJBException(e);
        }

        return returnText;
    }
}
