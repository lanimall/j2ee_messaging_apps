package com.softwareag.messaging.web;

import com.softwareag.messaging.utils.JMSHelper;
import com.softwareag.messaging.utils.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

/**
 * <p>
 * A simplistic (on purpose) servlet that sends JMS messages without  JCA construct (no connection pooling and the likes...)
 * </p>
 *
 * @author Fabien Sanglier
 */
@WebServlet("/SimpleNoJCAJmsProducer")
public class SimpleNonJCAJmsProducer extends BaseMessageProducer {
    private static final long serialVersionUID = 1L;
    private static Logger log = LoggerFactory.getLogger(SimpleNonJCAJmsProducer.class);

    @Override
    protected JMSHelper createMessageSender() throws ServletException {
        String destinationTypeName = AppConfig.getInstance().getPropertyHelper().getProperty("jms.destination.type", "queue");
        JMSHelper.DestinationType destinationType = JMSHelper.DestinationType.QUEUE;
        if ("topic".equalsIgnoreCase(destinationTypeName)) {
            destinationType = JMSHelper.DestinationType.TOPIC;
        } else if ("queue".equalsIgnoreCase(destinationTypeName)) {
            destinationType = JMSHelper.DestinationType.QUEUE;
        } else {
            throw new ServletException("jms.destination.type not valid.");
        }

        String destinationName = AppConfig.getInstance().getPropertyHelper().getProperty("jms.destination.name");
        if (null == destinationName)
            throw new ServletException("jms.destination.name not defined.");


        return JMSHelper.createSender(destinationName, destinationType);
    }
}
