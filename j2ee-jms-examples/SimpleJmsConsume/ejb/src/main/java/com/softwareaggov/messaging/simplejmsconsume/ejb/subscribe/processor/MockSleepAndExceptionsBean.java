package com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe.processor;

import com.softwareaggov.messaging.libs.jms.processor.MessageProcessor;
import com.softwareaggov.messaging.libs.jms.processor.impl.MockSleepAndExceptionsProcessor;
import com.softwareaggov.messaging.simplejmsconsume.ejb.utils.CounterLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

@Stateless(name = "MockSleepAndExceptionsProcessor")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(MessageProcessorLocal.class)
public class MockSleepAndExceptionsBean extends MockSleepBean implements MessageProcessorLocal {
    private static Logger log = LoggerFactory.getLogger(MockSleepAndExceptionsBean.class);

    @EJB
    private CounterLocal messageProcessingCounter;

    @Resource(name = "mockExceptionsCountInterval")
    private Integer mockExceptionsCountInterval = 0;

    @Resource(name = "mockSleepTimeInMillis")
    private Long mockSleepTimeInMillis = 0L;

    private MessageProcessor processor;

    @PostConstruct
    public void initialize() {
        processor = new MockSleepAndExceptionsProcessor(
                mockSleepTimeInMillis,
                mockExceptionsCountInterval,
                messageProcessingCounter);
    }

    @Override
    public Map.Entry<String, Map<String, String>> processMessage(Message msg) throws JMSException {
        return processor.processMessage(msg);
    }
}