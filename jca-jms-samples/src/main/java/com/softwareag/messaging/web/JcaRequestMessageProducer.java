package com.softwareag.messaging.web;

import com.softwareag.messaging.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 * A servlet that sends several JMS messages to a JMS queue or a topic
 * as defined by the jmsDestination variable that is bound to a JCA admin object (hence using JCA construct)
 * </p>
 * <p>
 * The servlet is registered and mapped to /JcaQueueProxyMessageProducer using the {@linkplain javax.servlet.annotation.WebServlet
 * @author Fabien Sanglier
 * @HttpServlet}. </p>
 */
@WebServlet("/JcaAsyncRequestReply")
public class JcaRequestMessageProducer extends BaseMessageProducer {
    private static final long serialVersionUID = -8314702649252239L;
    private static Logger log = LoggerFactory.getLogger(JcaRequestMessageProducer.class);

    @Resource(name = "jms/someManagedQCF")
    private ConnectionFactory connectionFactory;

    @Resource(name = "jms/someManagedRequestQueue")
    private Destination jmsDestination;

    @Resource(name = "jms/someManagedResponseQueue")
    private Destination jmsReplyTo;

    @Override
    protected JMSHelper createMessageSender() throws ServletException {
        return JMSHelper.createSender(connectionFactory, jmsDestination);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.write("<h1>Sending JMS message</h1>");
        try {
            int factor1 = rdm.nextInt(1000);
            int factor2 = rdm.nextInt(1000);

            String correlationId = JMSHelper.generateCorrelationID();
            String message = String.format("How much is %d * %d? [correlationID = %s]", factor1, factor2, correlationId);

            Map<String,String> headerProperties = new HashMap<String, String>(4);
            headerProperties.put("factor1", new Integer(factor1).toString());
            headerProperties.put("factor2", new Integer(factor2).toString());

            jmsHelper.sendMessage(messagePayload, headerProperties, correlationId, null, DeliveryMode.NON_PERSISTENT, 4);

            out.write(String.format("<p><i>%s</i></p>", message));
            out.write("<p><i>messages sent successfully</i></p>");
        } catch (JMSException e) {
            log.error("Error occurred", e);
            throw new ServletException(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
