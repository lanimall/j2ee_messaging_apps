package com.softwareaggov.messaging.service.endpoints.soap;

import com.softwareaggov.messaging.service.processors.RequestReplyProcessingBean;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jws.WebService;

/**
 * Created by fabien.sanglier on 5/2/17.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@WebService
        (
                serviceName = "RequestReplyHttpService",
                targetNamespace = "http://com.softwareaggov.messaging/j2ee-jms-examples/requestreply",
                endpointInterface = "com.softwareaggov.messaging.service.endpoints.soap.RequestReplyPortType",
                portName = "RequestReplyHttpPort",
                wsdlLocation = "META-INF/wsdl/RequestReplyHttpService.wsdl"
        )
public class RequestReplyHttpImpl extends RequestReplyProcessingBean implements RequestReplyPortType {
}