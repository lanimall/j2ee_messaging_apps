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

package com.softwareaggov.messaging.simplesoapjms.client.utils;

/**
 * Created by fabien.sanglier on 6/28/18.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.Set;

public class CdataHandler implements SOAPHandler<SOAPMessageContext> {
    private static Logger log = LoggerFactory.getLogger(CdataHandler.class);

    @Override
    public boolean handleMessage(SOAPMessageContext soapMessage) {
        log.debug("Client : handleMessage()......");

        try {
            SOAPMessage message = soapMessage.getMessage();
            boolean isOutboundMessage = (Boolean) soapMessage
                    .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

            // is a request?
            if (isOutboundMessage) {
                //get the raw content
                String xmlDocAsString = message.getSOAPBody().getFirstChild().getTextContent();

                // remove the xml content
                message.getSOAPBody().getFirstChild().removeChild(message.getSOAPBody().getFirstChild().getFirstChild());

                //set the CDATA content instead
                message.getSOAPBody().getFirstChild().setTextContent(
                        (new CdataAdapter()).marshal(xmlDocAsString)
                );

                message.saveChanges();
            }
        } catch (Exception ex) {
            log.error("Could not perform the handler operation", ex);
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        log.debug("Client : handleFault()......");
        return true;
    }

    @Override
    public void close(MessageContext context) {
        log.debug("Client : close()......");
    }

    @Override
    public Set<QName> getHeaders() {
        log.debug("Client : getHeaders()......");
        return Collections.EMPTY_SET;
    }
}