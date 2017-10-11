package com.softwareaggov.messaging.service.endpoints.jms.mdb.processor;

import com.softwareaggov.messaging.service.processor.CalculationProcessingLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Local(MDBProcessorLocal.class)
public class CalculationMDBProcessorBean implements MDBProcessorLocal {
    private static Logger log = LoggerFactory.getLogger(CalculationMDBProcessorBean.class);

    //invoke another business EJB for fun...
    @EJB(beanName = "CalculationProcessingBean", beanInterface = CalculationProcessingLocal.class)
    private CalculationProcessingLocal processorLocal;

    @Override
    public Map.Entry<String, Map<String, String>> processMessage(Message msg) throws JMSException {
        Map.Entry<String, Map<String, String>> processingResult = null;

        if (msg instanceof TextMessage) {
            String factor1 = msg.getStringProperty("factor1");
            String factor2 = msg.getStringProperty("factor2");

            //perform the business operation
            String opResult = processorLocal.performMultiplicationFromStrings(factor1, factor2);

            //Build response headers
            Map<String, String> responseHeaderProperties = new HashMap<String, String>(3);
            responseHeaderProperties.put("factor1", factor1);
            responseHeaderProperties.put("factor2", factor2);
            responseHeaderProperties.put("result", opResult);

            String payloadResponse = String.format("factor1: %s / factor2: %s = %s",
                    factor1,
                    factor2,
                    opResult
            );

            if (log.isDebugEnabled()) {
                log.debug("Response Payload Message: " + payloadResponse);
            }

            //create the processingResult pair
            processingResult = new AbstractMap.SimpleImmutableEntry<String, Map<String, String>>(
                    payloadResponse, responseHeaderProperties
            );
        } else {
            throw new EJBException("Message of wrong type: " + msg.getClass().getName());
        }

        return processingResult;
    }
}