package com.softwareaggov.messaging.web;

import com.softwareaggov.messaging.service.publish.JmsPublisherLocal;
import com.softwareaggov.messaging.utils.JMSHelper;
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
 * <p>
 * The servlet is registered and mapped to /JcaQueueProxyMessageProducer using the {@linkplain javax.servlet.annotation.WebServlet
 * @author Fabien Sanglier
 * @HttpServlet}. </p>
 */
@WebServlet("/JcaAsyncRequestReply")
public class JcaRequestMessageProducer extends BaseMessageProducer {
    private static final long serialVersionUID = -8314702649252239L;
    private static Logger log = LoggerFactory.getLogger(JcaRequestMessageProducer.class);

    @EJB(beanName = "JmsManagedRequestReplyPublisherBean") //here specify the bean name because I have multiple bean for the same interface
    private JmsPublisherLocal jmsRequestReplyPublisher;

    @EJB(beanName = "JmsManagedRequestReplyCachedPublisherBean") //here specify the bean name because I have multiple bean for the same interface
    private JmsPublisherLocal jmsRequestReplyCachedPublisher;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        boolean useCached = Boolean.parseBoolean(req.getParameter("useCachedConnection"));

        try {
            out.write("<h1>Sending JMS message</h1>");
            int factor1 = rdm.nextInt(1000);
            int factor2 = rdm.nextInt(1000);

            String correlationId = JMSHelper.generateCorrelationID();
            String message = String.format("How much is %d * %d? [correlationID = %s]", factor1, factor2, correlationId);

            Map<String,String> headerProperties = new HashMap<String, String>(4);
            headerProperties.put("factor1", new Integer(factor1).toString());
            headerProperties.put("factor2", new Integer(factor2).toString());

            if(useCached)
                jmsRequestReplyCachedPublisher.sendTextMessage(messagePayload, headerProperties);
            else
                jmsRequestReplyPublisher.sendTextMessage(messagePayload, headerProperties);

            out.write(String.format("<p><i>%s</i></p>", message));
            out.write("<p><i>messages sent successfully</i></p>");
        } catch (Exception exc){
            out.write(String.format("<p>An error occurred:%s</p>",exc.getMessage()));
            exc.printStackTrace(out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
