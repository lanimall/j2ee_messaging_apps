

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

package com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe;


import com.softwareaggov.messaging.libs.jms.processor.ProcessorOutput;
import com.softwareaggov.messaging.libs.utils.JMSHelper;
import com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe.processor.MessageProcessorLocal;
import com.softwareaggov.messaging.libs.utils.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * A simple Message Driven Bean that receives and processes messages through JMS constructs from defined JMS destinations
 * </p>
 *
 * @author Fabien Sanglier
 */

@MessageDriven(name = "MessageConsumerService")
@TransactionManagement(value = TransactionManagementType.BEAN)
public class MessageConsumerServiceBean implements MessageListener, MessageDrivenBean {
    private static final long serialVersionUID = -4602751473208935601L;
    private static Logger log = LoggerFactory.getLogger(MessageConsumerServiceBean.class);

    @EJB(beanName = "CounterService")
    protected Counter messageProcessingCounter;

    //Implementation for the message processing
    @EJB(name = "ejb/messageProcessor")
    private MessageProcessorLocal messageProcessor;

    private MessageDrivenContext mdbContext;

    private static final String RESOURCE_NAME_REPLYCF = "jms/someManagedReplyCF";
    private static final String RESOURCE_NAME_REPLYDEST = "jms/someManagedDefaultReplyTo";

    @Resource(name = "jmsMessageEnableReply")
    private Boolean jmsMessageEnableReply = false;

    @Resource(name = "jmsMessageReplyOverridesDefault")
    private Boolean jmsMessageReplyOverridesDefault = true;

    @Resource(name = "jmsMessageReplySessionTransacted")
    private Boolean jmsMessageReplySessionTransacted = Boolean.FALSE;

    @Resource(name = "jmsMessageReplySessionAcknowledgeMode")
    private Integer jmsMessageReplySessionAcknowledgeMode = Session.AUTO_ACKNOWLEDGE;

    //resource looked up from JNDI
    private ConnectionFactory jmsReplyConnectionFactory = null;

    //resource looked up from JNDI
    private Destination jmsDefaultReplyTo;

    @PostConstruct
    public void ejbCreate() {
        log.info("ejbCreate()");

        //lookup the reply resources, and set null if resource cannot be found
        if (jmsMessageEnableReply) {
            log.info("jmsMessageEnableReply=true -- trying to lookup the reply resources");

            try {
                jmsReplyConnectionFactory = (ConnectionFactory) lookupEnvResource(RESOURCE_NAME_REPLYCF);
            } catch (NamingException e) {
                log.error("Could not lookup the resource " + RESOURCE_NAME_REPLYCF + ". Reply functionality will not be enabled. This could be expected if indeed reply functionality is turned off...check configs if in doubt.", e);
            }

            try {
                jmsDefaultReplyTo = (Destination) lookupEnvResource(RESOURCE_NAME_REPLYDEST);
            } catch (NamingException e) {
                log.error("Could not lookup the resource " + RESOURCE_NAME_REPLYDEST + ". Reply functionality will not be enabled. This could be expected if indeed reply functionality is turned off...check configs if in doubt.", e);
            }
        }

        messageProcessingCounter.incrementAndGet(getBeanName() + "-create");
    }

    @PreDestroy
    public void ejbRemove() throws EJBException {
        log.info("ejbRemove()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-remove");
    }

    public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException {
        mdbContext = ctx;
    }

    protected String getBeanName() {
        return this.getClass().getSimpleName();
    }

    public Object lookupEnvResource(String jndiLookupName) throws NamingException {
        // create the context
        final Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup("java:comp/env");
        return envCtx.lookup(jndiLookupName);
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message msg) {
        if (log.isDebugEnabled())
            log.debug("onMessage() start");

        messageProcessingCounter.incrementAndGet(getBeanName() + "-messageConsumed");

        if (null != msg) {
            //post processing result
            ProcessorOutput processorResult = null;
            try {
                if (log.isDebugEnabled() || log.isTraceEnabled()) {
                    Object preProcessingPayload = JMSHelper.getMessagePayload(msg);
                    String preProcessingJMSHeaderPropertiesStr = JMSHelper.getMessageJMSHeaderPropsAsString(msg, ",");
                    String preProcessingCustomPropertiesStr = JMSHelper.getMessagePropertiesAsString(msg, ",");

                    if (log.isTraceEnabled()) {
                        log.trace("Received message before any processing: {}, \nJMS Headers: {}, \nCustom Properties: {}",
                                ((null != preProcessingPayload) ? preProcessingPayload : "null"),
                                ((null != preProcessingJMSHeaderPropertiesStr) ? preProcessingJMSHeaderPropertiesStr : "null"),
                                ((null != preProcessingCustomPropertiesStr) ? preProcessingCustomPropertiesStr : "null"));
                    } else if (log.isDebugEnabled()) {
                        log.debug("Received message before any processing with JMS Headers: {}, \nCustom Properties: {}",
                                ((null != preProcessingJMSHeaderPropertiesStr) ? preProcessingJMSHeaderPropertiesStr : "null"),
                                ((null != preProcessingCustomPropertiesStr) ? preProcessingCustomPropertiesStr : "null"));
                    }
                }

                //processing the message
                if (null == messageProcessor)
                    throw new IllegalArgumentException("Message Processor is null...unexpected.");

                //process the message
                processorResult = messageProcessor.processMessage(msg);

                if (log.isDebugEnabled() || log.isTraceEnabled()) {
                    if (null != processorResult) {
                        if (log.isTraceEnabled()) {
                            log.trace("Post processing payload: {}, \nJMS Headers: {}, \nCustom Properties: {}",
                                    ((null != processorResult.getMessagePayload()) ? processorResult.getMessagePayload() : "null"),
                                    ((null != processorResult.getJMSHeaderProperties()) ? JMSHelper.getMessageJMSHeaderPropsAsString(processorResult.getJMSHeaderProperties(), ",") : "null"),
                                    ((null != processorResult.getMessageProperties()) ? JMSHelper.getMessagePropertiesAsString(processorResult.getMessageProperties(), ",") : "null"));
                        } else if (log.isDebugEnabled()) {
                            log.debug("Post processing payload with JMS Headers: {}, \nCustom Properties: {}",
                                    ((null != processorResult.getJMSHeaderProperties()) ? JMSHelper.getMessageJMSHeaderPropsAsString(processorResult.getJMSHeaderProperties(), ",") : "null"),
                                    ((null != processorResult.getMessageProperties()) ? JMSHelper.getMessagePropertiesAsString(processorResult.getMessageProperties(), ",") : "null"));
                        }
                    } else {
                        log.debug("Post processing payload is NULL");
                    }
                }

                messageProcessingCounter.incrementAndGet(getBeanName() + "-processingSuccess");
            } catch (Exception e) {
                log.error("Exception occurred", e);
                messageProcessingCounter.incrementAndGet(getBeanName() + "-processingErrors");
                throw new EJBException(e);
            }

            if (jmsMessageEnableReply && null != processorResult) {
                try {
                    Object postProcessingPayload = null;
                    Map<JMSHelper.JMSHeadersType, Object> postProcessingJMSHeaderProperties = null;
                    Map<String, Object> postProcessingCustomProperties = null;
                    if (null != processorResult) {
                        postProcessingPayload = processorResult.getMessagePayload();
                        postProcessingJMSHeaderProperties = processorResult.getJMSHeaderProperties();
                        postProcessingCustomProperties = processorResult.getMessageProperties();
                    }

                    //if replyTo overrides default, use it first, and only use default if the message replyTo is null
                    //otherwise, force to using the default always
                    Destination replyTo = null;
                    if (jmsMessageReplyOverridesDefault) {
                        if (null != postProcessingJMSHeaderProperties && null != postProcessingJMSHeaderProperties.get(JMSHelper.JMSHeadersType.JMS_REPLYTO))
                            replyTo = (Destination) postProcessingJMSHeaderProperties.get(JMSHelper.JMSHeadersType.JMS_REPLYTO);

                        if (null == replyTo)
                            replyTo = jmsDefaultReplyTo;
                    } else {
                        replyTo = jmsDefaultReplyTo;
                    }

                    //if replyTo is set, reply
                    if (null != replyTo && null != jmsReplyConnectionFactory) {
                        if (null == postProcessingJMSHeaderProperties)
                            postProcessingJMSHeaderProperties = new HashMap<JMSHelper.JMSHeadersType, Object>();

                        //set the destination to the replyTo
                        postProcessingJMSHeaderProperties.put(JMSHelper.JMSHeadersType.JMS_DESTINATION, replyTo);

                        //clear replyTo field to avoid infinite loops...
                        postProcessingJMSHeaderProperties.remove(JMSHelper.JMSHeadersType.JMS_REPLYTO);

                        // Get correlationID from message if set.
                        // If not set, then get the MessageID from message, and set the reply correlationID with it
                        if (null == postProcessingJMSHeaderProperties.get(JMSHelper.JMSHeadersType.JMS_CORRELATIONID))
                            postProcessingJMSHeaderProperties.put(JMSHelper.JMSHeadersType.JMS_CORRELATIONID, postProcessingJMSHeaderProperties.get(JMSHelper.JMSHeadersType.JMS_MESSAGEID));

                        //send reply
                        JMSHelper.createSender(jmsReplyConnectionFactory).sendAndForgetTextMessage(
                                postProcessingPayload,
                                jmsMessageReplySessionTransacted,
                                jmsMessageReplySessionAcknowledgeMode,
                                postProcessingJMSHeaderProperties,
                                postProcessingCustomProperties);

                        messageProcessingCounter.incrementAndGet(getBeanName() + "-replySuccess");
                    } else {
                        if(null == jmsReplyConnectionFactory){
                            messageProcessingCounter.incrementAndGet(getBeanName() + "-replyNullConnectionFactory");
                            log.warn("Reply can't be sent because jmsReplyConnectionFactory is null");
                        }

                        if(null == replyTo) {
                            messageProcessingCounter.incrementAndGet(getBeanName() + "-replyNullDestination");
                            log.warn("Reply can't be sent because replyTo is null");
                        }
                    }
                } catch (Exception e) {
                    log.error("Exception occurred", e);
                    messageProcessingCounter.incrementAndGet(getBeanName() + "-replyErrors");
                    throw new EJBException(e);
                }
            }
        } else {
            messageProcessingCounter.incrementAndGet(getBeanName() + "-messageConsumedNull");
            if (log.isWarnEnabled())
                log.warn("Received Message from queue: null");
        }

    }
}