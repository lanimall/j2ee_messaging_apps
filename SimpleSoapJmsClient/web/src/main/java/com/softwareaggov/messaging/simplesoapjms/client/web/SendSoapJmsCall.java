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

package com.softwareaggov.messaging.simplesoapjms.client.web;

import com.softwareaggov.messaging.simplesoapjms.client.ejb.SoapClientLocal;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by fabien.sanglier on 6/20/18.
 */
@WebServlet("/SendSoapJms")
public class SendSoapJmsCall extends HttpServlet {
    private static final long serialVersionUID = -8314702394752239L;
    private static Logger log = LoggerFactory.getLogger(SendSoapJmsCall.class);

    @EJB(beanName = "SoapJmsClientOneWayService")
    private SoapClientLocal soapJmsClientOneWayService;

    @EJB(beanName = "SoapJmsClientTwoWayService")
    private SoapClientLocal soapJmsClientTwoWayService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.write("<h1>" + req.getContextPath() + " - Sending SOAP Call</h1>");
        try {
            String callType = req.getParameter("calltype");
            SoapClientLocal soapJmsService = null;
            if(null != callType){
                if("oneway".equals(callType.toLowerCase()))
                    soapJmsService = soapJmsClientOneWayService;
                else if ("twoway".equals(callType.toLowerCase()))
                    soapJmsService = soapJmsClientTwoWayService;
                else
                    throw new IllegalArgumentException("call type [" + callType + "] not supported.");
            } else {
                throw new IllegalArgumentException("call type [null] not supported.");
            }

            if (null == soapJmsService)
                throw new IllegalArgumentException("soapJmsService is null...should not be...check code or configs");

            String response = soapJmsService.callWS();

            String responseToDisplay = "";
            if(null != response) {
                if(response.toLowerCase().startsWith("<?xml")) {
                    responseToDisplay = StringEscapeUtils.escapeXml10(response);
                } else {
                    responseToDisplay = response;
                }
            } else {
                responseToDisplay = "null";
            }

            out.write("<p><b>messages sent successfully</b></p>");
            out.write(String.format("<div><p>Response:</p><p>%s</p></div>", responseToDisplay));

            out.close();
        } catch (Throwable exc) {
            log.error("Error Occurred", exc);
            throw new ServletException(exc);
        }
    }
}
