package com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe.processor;

import com.softwareaggov.messaging.libs.jms.processor.MessageProcessor;
import com.softwareaggov.messaging.libs.jms.processor.impl.MockSleepProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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

@Stateless(name = "MockSleepProcessor")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(MessageProcessorLocal.class)
public class MockSleepBean implements MessageProcessorLocal {
    private static Logger log = LoggerFactory.getLogger(MockSleepBean.class);

    @Resource(name = "mockSleepTimeInMillis")
    private Long mockSleepTimeInMillis = 0L;

    private MessageProcessor processor;

    @PostConstruct
    public void initialize() {
        processor = new MockSleepProcessor(mockSleepTimeInMillis);
    }

    @Override
    public Map.Entry<String, Map<String, Object>> processMessage(Message msg) throws JMSException {
        return processor.processMessage(msg);
    }
}