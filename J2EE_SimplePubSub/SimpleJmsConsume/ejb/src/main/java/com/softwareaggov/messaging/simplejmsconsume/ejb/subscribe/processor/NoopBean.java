package com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe.processor;

import com.softwareaggov.messaging.libs.jms.processor.MessageProcessor;
import com.softwareaggov.messaging.libs.jms.processor.impl.NoopProcessor;

import javax.annotation.PostConstruct;
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

@Stateless(name = "NoopProcessor")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(MessageProcessorLocal.class)
public class NoopBean implements MessageProcessorLocal {

    private MessageProcessor processor;

    @PostConstruct
    public void initialize() {
        processor = new NoopProcessor();
    }

    @Override
    public Map.Entry<String, Map<String, Object>> processMessage(Message msg) throws JMSException {
        return processor.processMessage(msg);
    }
}