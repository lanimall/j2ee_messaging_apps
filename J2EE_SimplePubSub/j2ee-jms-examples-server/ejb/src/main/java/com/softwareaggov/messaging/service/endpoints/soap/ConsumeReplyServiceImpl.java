package com.softwareaggov.messaging.service.endpoints.soap;

import com.softwareaggov.messaging.service.processor.CalculationProcessingLocal;

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
                serviceName = "ConsumeReplyService",
                targetNamespace = "http://com.softwareaggov.messaging/j2ee-jms-examples/consumereply",
                endpointInterface = "com.softwareaggov.messaging.service.endpoints.soap.ConsumeReplyServicePortType",
                portName = "ConsumeReplyServicePort"
        )
public class ConsumeReplyServiceImpl implements ConsumeReplyServicePortType {
    @EJB(beanName = "CalculationProcessingBean", beanInterface = CalculationProcessingLocal.class)
    private CalculationProcessingLocal processorLocal;

    @Override
    public String performMultiplicationFromStrings(String nb1, String nb2) {
        return processorLocal.performMultiplicationFromStrings(nb1, nb2);
    }

    @Override
    public Long getRandomNumber() {
        return processorLocal.getRandomNumber();
    }
}