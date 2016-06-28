package com.softwareaggov.messaging.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@Local(MessageProcessingLocal.class)
public class MessageProcessingBean implements MessageProcessingLocal {
    private static Logger log = LoggerFactory.getLogger(MessageProcessingBean.class);

    @Override
    public String stringifyMessageProperties(Message msg, String ... propNames) {
        if(log.isDebugEnabled())
            log.debug("MessageProcessingLocal: stringifyMessageProperties()");

        StringBuilder logText = new StringBuilder();
        try {
            if(null != propNames) {
                for (String propName : propNames) {
                    if (logText.length() > 0) logText.append(",");
                    logText.append(String.format("JMS_Property[%s]=%s", propName, (null != msg.getObjectProperty(propName)) ? msg.getObjectProperty(propName).toString() : "null"));
                }
            }
        } catch (JMSException e) {
            log.warn("Cannot read the message properties...", e);
        }
        return logText.toString();
    }
}