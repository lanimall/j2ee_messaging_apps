package com.softwareaggov.messaging.service.endpoints.jms.mdb.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.AbstractMap;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Local(MDBProcessorLocal.class)
public class MockSleepMDBProcessorBean implements MDBProcessorLocal {
    private static Logger log = LoggerFactory.getLogger(MockSleepMDBProcessorBean.class);

    @Override
    public Map.Entry<String, Map<String, String>> processMessage(Message msg) throws JMSException {
        Map.Entry<String, Map<String, String>> processingResult = null;

        long sleepTime = 100L;
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            log.error("interrupt exception", e);
        }

        String payloadResult = String.format("Slept for %d ms", sleepTime);

        //create the processingResult pair
        processingResult = new AbstractMap.SimpleImmutableEntry<String, Map<String, String>>(
                payloadResult, null
        );

        return processingResult;
    }
}