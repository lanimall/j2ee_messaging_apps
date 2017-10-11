package com.softwareaggov.messaging.service.endpoints.jms.mdb.processor;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Local(MDBProcessorLocal.class)
public class NoopMDBProcessorBean implements MDBProcessorLocal {

    @Override
    public Map.Entry<String, Map<String, String>> processMessage(Message msg) throws JMSException {
        return null;
    }
}