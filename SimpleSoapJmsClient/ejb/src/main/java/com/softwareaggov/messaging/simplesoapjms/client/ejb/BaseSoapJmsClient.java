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

package com.softwareaggov.messaging.simplesoapjms.client.ejb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

/**
 * Created by fabien.sanglier on 6/20/18.
 */
public abstract class BaseSoapJmsClient extends BaseSoapClient {
    private static Logger log = LoggerFactory.getLogger(BaseSoapJmsClient.class);

    private final static String WSDLPATH = "classpath:/META-INF/wsdl/TestWebService_SoapJms.wsdl";

    //TODO: these resources are not really needed...but creating them somehow makes the link for this app to load the RA too (and avoid ClassNotFound errors)
    @Resource(name = "jms/someManagedCF")
    private ConnectionFactory jmsConnectionFactory;

    @Resource(name = "jms/someManagedDestination")
    private Destination jmsDestination;

    @Override
    @PostConstruct
    public void ejbCreate() {
        super.ejbCreate();
    }

    @Override
    @PreDestroy
    public void ejbRemove() throws EJBException {
        super.ejbRemove();
    }

    @Override
    String getServiceWsdlPath() {
        return WSDLPATH;
    }
}
