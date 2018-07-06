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

import com.softwareaggov.messaging.libs.utils.Counter;
import com.softwareaggov.messaging.libs.utils.FileUtils;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsPublisher;
import com.softwareaggov.messaging.simplesoapjms.client.jaxws.TestWebService;
import com.softwareaggov.messaging.simplesoapjms.client.jaxws.TestWebServicePortType;
import com.softwareaggov.messaging.simplesoapjms.client.utils.HandlerFactory;
import com.softwareaggov.messaging.simplesoapjms.client.utils.SoapLoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.jms.JMSException;
import javax.xml.ws.BindingProvider;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/27/18.
 */
public abstract class BaseSoapClient implements SoapClient, JmsPublisher {
    private static Logger log = LoggerFactory.getLogger(BaseSoapClient.class);

    @EJB(beanName = "CounterService")
    protected Counter messageProcessingCounter;

    @Resource(name = "isEnabled")
    private Boolean isEnabled;

    @Resource(name = "msgPayloadFilePath")
    private String msgPayloadFilePath;

    @Resource(name = "soapEndpointUrl")
    private String soapEndpointUrl;

    protected TestWebService testWebService;
    protected TestWebServicePortType testWebServicePort;
    protected transient String msgPayload;

    protected String getBeanName() {
        return this.getClass().getSimpleName();
    }

    abstract String getServiceWsdlPath();

    public void ejbCreate() {
        log.info("ejbCreate()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-create");

        URL wsdlUrl;
        try {
            wsdlUrl = FileUtils.getFileURL(getServiceWsdlPath());
        } catch (MalformedURLException e) {
            throw new EJBException("Could not get the WSDL from path " + getServiceWsdlPath(), e);
        }

        testWebService = new TestWebService(wsdlUrl);
        testWebServicePort = testWebService.getTestWebServicePort();

        //set handlers
        testWebService.setHandlerResolver(HandlerFactory.build(new SoapLoggingHandler()));

        //set endpoint url
        if(null != soapEndpointUrl && !"".equals(soapEndpointUrl)) {
            log.info("Trying to set a new endpoint url to: " + soapEndpointUrl);
            ((BindingProvider) testWebServicePort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, soapEndpointUrl);
        }

        try {
            msgPayload = FileUtils.getFileContent(msgPayloadFilePath);
        } catch (IOException e) {
            throw new EJBException("Could not get the file content from path " + msgPayloadFilePath, e);
        }
    }

    public void ejbRemove() throws EJBException {
        log.info("ejbRemove()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-remove");
    }

    public String callWS(){
        return callWS(msgPayload);
    }

    public abstract String callWS(String content);

    @Override
    public String sendTextMessage(Object msgTextPayload, Map<String, Object> msgHeaderProperties) throws JMSException {
        return callWS((null != msgTextPayload)?msgTextPayload.toString():  msgPayload);
    }

    @Override
    public boolean isEnabled() {
        return (null != isEnabled) ? isEnabled : false;
    }
}
