package com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe.processor;

import com.softwareaggov.messaging.libs.jms.processor.MessageProcessor;
import com.softwareaggov.messaging.libs.jms.processor.impl.MessageCloneProcessor;
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
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

@Stateless(name = "MockSleepProcessor")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(MessageProcessorLocal.class)
public class MessageCloneBean implements MessageProcessorLocal {
    private static Logger log = LoggerFactory.getLogger(MessageCloneBean.class);

    private static final String PROPS_KEYVAL_DELIM = "=";
    private static final String PROPS_DELIM = ";";

    @Resource(name = "msgPayloadOverride")
    private String msgPayloadOverride;

    @Resource(name = "msgPropertiesOverride")
    private String msgPropertiesOverride;

    @Resource(name = "msgPayloadOverride")
    private Boolean overwriteAllProperties;

    private MessageProcessor processor;

    @PostConstruct
    public void initialize() {
        //need to parse message properties from a string into a map, following format: key1=val1;key2=val2;key3=val3
        Map<String, String> props = new HashMap<>();
        if (null != msgPropertiesOverride && !"".equals(msgPropertiesOverride)) {
            StringTokenizer st = new StringTokenizer(msgPropertiesOverride, PROPS_DELIM, false);

            String propPairStr;
            String[] propPair;
            while (st.hasMoreTokens()) {
                propPairStr = st.nextToken();
                propPair = propPairStr.split(PROPS_KEYVAL_DELIM);
                if (propPair.length == 1)
                    props.put(propPair[0], null);
                else if (propPair.length > 1)
                    props.put(propPair[0], propPair[1]);
            }
        }

        processor = new MessageCloneProcessor(msgPayloadOverride, props, overwriteAllProperties);
    }

    @Override
    public Map.Entry<String, Map<String, Object>> processMessage(Message msg) throws JMSException {
        return processor.processMessage(msg);
    }
}