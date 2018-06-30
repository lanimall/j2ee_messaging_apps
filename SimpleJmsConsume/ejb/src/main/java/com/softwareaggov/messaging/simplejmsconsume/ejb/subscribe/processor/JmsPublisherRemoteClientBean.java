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
 */subscribe.processor;

import com.softwareaggov.messaging.libs.jms.processor.ProcessorOutput;
import com.softwareaggov.messaging.libs.jms.processor.impl.ProcessorOutputImpl;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsPublisherRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by fabien.sanglier on 6/26/18.
 */

@Stateless(name = "JmsPublisherRemoteClientProcessor")
@Local(MessageProcessorLocal.class)
@TransactionManagement(value = TransactionManagementType.BEAN)
public class JmsPublisherRemoteClientBean implements MessageProcessorLocal {
    private static Logger log = LoggerFactory.getLogger(JmsPublisherRemoteClientBean.class);

    // This will be injected dynamically by jndi lookup...
    // the reason is that we don't want the deployment to fail if the jmsMessagePublisher is not set AND this bean is not used in the runtime path
    private JmsPublisherRemote jmsMessagePublisher;

    //generally global based on target deployment
    @Resource(name = "jndi.ejblookup.initialContextFactory")
    private String jndiInitialContextFactory;

    @Resource(name = "jndi.ejblookup.urlPackagePrefix")
    private String jndiUrlPackagePrefix;

    //generally based on where to access the beans
    @Resource(name = "jndi.ejblookup.url")
    private String jndiUrl;

    //Generally, format for EJB lookup is: "ejb:" + appName + "/" + moduleName + "/" + beanName + "!" + viewClassName;
    @Resource(name = "jndi.ejblookup.bindingname")
    private String jndiEjbLookupBindingName;

    @PostConstruct
    public void initialize() {
        if (null != jndiEjbLookupBindingName && !"".equals(jndiEjbLookupBindingName)) {
            final Properties jndiProperties = new Properties();
            if (null != jndiUrl && !"".equals(jndiUrl))
                jndiProperties.put(Context.PROVIDER_URL, jndiUrl);

            if (null != jndiInitialContextFactory && !"".equals(jndiInitialContextFactory))
                jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, jndiInitialContextFactory);

            if (null != jndiUrlPackagePrefix && !"".equals(jndiUrlPackagePrefix))
                jndiProperties.put(Context.URL_PKG_PREFIXES, jndiUrlPackagePrefix);

            // create the context
            final Context context;
            try {
                context = new InitialContext(jndiProperties);
                jmsMessagePublisher = (JmsPublisherRemote) context.lookup(jndiEjbLookupBindingName);
            } catch (NamingException e) {
                log.error("Could not lookup the EJB with URL:" + jndiEjbLookupBindingName, e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public ProcessorOutput processMessage(Message msg) throws JMSException {
        if (msg instanceof TextMessage) {

            TextMessage txtMsg = (TextMessage) msg;

            if (null != jmsMessagePublisher) {
                //copy the properties from the incoming message
                HashMap props = new HashMap();
                Enumeration txtMsgPropertiesEnum = txtMsg.getPropertyNames();
                while (txtMsgPropertiesEnum.hasMoreElements()) {
                    String propName = (String) txtMsgPropertiesEnum.nextElement();
                    props.put(propName, txtMsg.getObjectProperty(propName));
                }

                //send the send and wait message
                String textReturned = jmsMessagePublisher.sendTextMessage(txtMsg.getText(), Collections.unmodifiableMap(props));

                // Packaging the payload + properties into processorOutput object
                return new ProcessorOutputImpl(
                        textReturned,
                        null,
                        Collections.unmodifiableMap(props)
                );
            } else {
                throw new IllegalArgumentException("jmsMessagePublisher is null...cannot do anything");
            }
        } else {
            throw new IllegalArgumentException("Received non-text message");
        }
    }
}