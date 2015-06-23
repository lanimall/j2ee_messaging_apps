package com.github.lanimall.messaging.samples.jmsgenerator.app;

import com.github.lanimall.messaging.samples.jmsgenerator.pojos.CreditCardTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by FabienSanglier on 6/16/15.
 */

@Service
@PropertySource("classpath:application.properties")
public class MsgPublisher {
    final Logger log = LoggerFactory.getLogger(this.getClass());

    private static String EVENT_TYPE = "eventType";

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    ResourceLoader resourceLoader;

    @Scheduled(fixedDelayString = "${com.softwareag.jmsgenerator.goodtransactions.fixedDelay.milliseconds}", initialDelay = 1000)
    public void publishGoodMessage() {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put(EVENT_TYPE, "transaction");
        properties.put("posTransaction", "normal");

        CreditCardTransaction tx = CreditCardTransaction.generateRandomNormalTransaction();
        properties.putAll(tx.toProperties());

        StringBuffer sb = new StringBuffer();
        for(Map.Entry<String, String> entry : properties.entrySet()){
            if(sb.length() > 0)
                sb.append(",");
            sb.append(entry.getValue());
        }

        sendMessage(sb.toString(), properties);
    }

    @Scheduled(fixedDelayString = "${com.softwareag.jmsgenerator.anomalies.fixedDelay.milliseconds}", initialDelay = 10000)
    public void publishAnomaly() {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put(EVENT_TYPE, "transaction");
        properties.put("posTransaction", "anomoly");

        CreditCardTransaction tx = CreditCardTransaction.generateRandomAnomalyTransaction();
        properties.putAll(tx.toProperties());

        StringBuffer sb = new StringBuffer();
        for(Map.Entry<String, String> entry : properties.entrySet()){
            if(sb.length() > 0)
                sb.append(",");
            sb.append(entry.getValue());
        }

        sendMessage(sb.toString(), properties);
    }

    public void sendMessage(final String messageContent, final Map<String,String> properties){
        if(log.isDebugEnabled()){
            log.debug("Sending message:" + messageContent);
        }

        try {
            sendTextMessage(messageContent, properties);
        } catch (JMSException e) {
            log.error("Could not send the event onto the bus...error.", e);
        }
    }

    public void sendTextMessage(final String message, final Map<String,String> properties) throws JMSException {
        jmsTemplate.send(new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                Message createMessage = session.createTextMessage(message);
                if(null != properties){
                    for(Map.Entry<String, String> entry : properties.entrySet()){
                        createMessage.setStringProperty(entry.getKey(),entry.getValue());
                    }
                }
                if(log.isDebugEnabled())
                    log.debug("Sending Message Notification:" + createMessage);
                return createMessage;
            }
        });
    }
}
