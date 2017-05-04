package com.softwareaggov.messaging.service.endpoints.soap;

import com.softwareaggov.messaging.service.processors.RequestReplyProcessingLocal;

import javax.ejb.EJB;
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
                serviceName = "RequestReplyService",
                targetNamespace = "http://com.softwareaggov.messaging/j2ee-jms-examples/requestreply",
                endpointInterface = "com.softwareaggov.messaging.service.endpoints.soap.RequestReplyServicePortType",
                portName = "RequestReplyServicePort"
        )
public class RequestReplyServiceImpl implements RequestReplyServicePortType {
    @EJB(beanName = "RequestReplyProcessingBean", beanInterface = RequestReplyProcessingLocal.class)
    private RequestReplyProcessingLocal requestReplyProcessing;

    @Override
    public String performMultiplicationFromStrings(String nb1, String nb2) {
        return requestReplyProcessing.performMultiplicationFromStrings(nb1,nb2);
    }

    @Override
    public Long getRandomNumber() {
        return requestReplyProcessing.getRandomNumber();
    }
}