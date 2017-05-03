package com.softwareaggov.messaging.service.endpoints.soap;

import com.softwareaggov.messaging.service.processors.RequestReplyProcessingBean;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jws.WebService;
import javax.xml.ws.BindingType;

/**
 * Created by fabien.sanglier on 5/2/17.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@WebService
        (
                serviceName = "RequestReplyJmsService",
                targetNamespace = "http://com.softwareaggov.messaging/j2ee-jms-examples/requestreply",
                endpointInterface = "com.softwareaggov.messaging.service.endpoints.soap.RequestReplyPortType",
                portName = "RequestReplyJmsPort",
                wsdlLocation = "META-INF/wsdl/RequestReplyJmsService.wsdl"
        )
@BindingType("http://www.w3.org/2010/soapjms/")
public class RequestReplyJMSImpl extends RequestReplyProcessingBean implements RequestReplyPortType {
//    @EJB(beanName = "RequestReplyProcessingBean", beanInterface = RequestReplyProcessing.class)
//    private RequestReplyProcessing requestReplyProcessing;
//
//    @Override
//    public String performMultiplicationFromStrings(String nb1, String nb2) {
//        return requestReplyProcessing.performMultiplicationFromStrings(nb1, nb2);
//    }
//
//    @Override
//    public Long getRandomNumber() {
//        return requestReplyProcessing.getRandomNumber();
//    }
}
