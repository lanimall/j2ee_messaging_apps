package com.softwareaggov.messaging.web;

import com.softwareaggov.messaging.service.publish.JmsPublisherLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * A servlet that sends several JMS messages to a JMS queue or a topic
 * as defined by the jmsDestination variable that is bound to a JCA admin object (hence using JCA construct)
 * </p>
 * <p/>
 * The servlet is registered and mapped to /JcaQueueProxyMessageProducer using the {@linkplain javax.servlet.annotation.WebServlet
 *
 * @author Fabien Sanglier
 * @HttpServlet}. </p>
 */
@WebServlet("/MessageProducerOneWay2")
public class MessageProducerOneWay2 extends BaseMessageProducer {
    private static final long serialVersionUID = -8314702649252239L;
    private static Logger log = LoggerFactory.getLogger(MessageProducerOneWay2.class);

    @EJB(beanName = "JmsManagedOneWayTopicPublisherBean")
    //here specify the bean name because I have multiple bean for the same interface
    private JmsPublisherLocal jmsSimplePublisher;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.write("<h1>Sending JMS message To Topic</h1>");
        try {
            int randomNumber = rdm.nextInt();
            String message = String.format("This is a text message with random number: %d", randomNumber);

            Map<String, String> headerProperties = new HashMap<String, String>(4);
            headerProperties.put("number_property", new Integer(randomNumber).toString());

            jmsSimplePublisher.sendTextMessage(messagePayload, headerProperties);

            out.write(String.format("<p><i>%s</i></p>", message));
            out.write("<p><b>messages sent successfully</b></p>");
            out.close();
        } catch (Throwable exc) {
            log.error("Error Occurred", exc);
            throw new ServletException(exc);
        }
    }
}
