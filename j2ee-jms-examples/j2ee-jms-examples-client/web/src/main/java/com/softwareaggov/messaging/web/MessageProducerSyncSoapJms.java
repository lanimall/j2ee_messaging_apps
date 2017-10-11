package com.softwareaggov.messaging.web;

import com.softwareaggov.messaging.service.publish.soap.RequestReplyClientLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

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
@WebServlet("/MessageProducerSyncSoapJms")
public class MessageProducerSyncSoapJms extends BaseMessageProducer {
    private static final long serialVersionUID = -8314702649252239L;
    private static Logger log = LoggerFactory.getLogger(MessageProducerSyncSoapJms.class);

    @EJB(beanName = "RequestReplySoapHttpClientBean")
    //here specify the bean name because I have multiple bean for the same interface
    private RequestReplyClientLocal requestReplyWithSoapHttp;

    @EJB(beanName = "RequestReplySoapJmsClientBean")
    //here specify the bean name because I have multiple bean for the same interface
    private RequestReplyClientLocal requestReplyWithSoapJms;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        boolean useSoapJMS = Boolean.parseBoolean(req.getParameter("useSoapJMS"));

        try {
            out.write("<h1>Sending Internal SOAP requests:</h1>");
            out.write("<h2>Request 1</h2>");

            //request 1
            int factor1 = rdm.nextInt();
            int factor2 = rdm.nextInt();
            String result = "";
            if (useSoapJMS)
                result = requestReplyWithSoapJms.performMultiplicationFromStrings(new Integer(factor1).toString(), new Integer(factor2).toString());
            else
                result = requestReplyWithSoapHttp.performMultiplicationFromStrings(new Integer(factor1).toString(), new Integer(factor2).toString());

            String message = String.format("How much is %d * %d? --> Response: %s", factor1, factor2, result);
            out.write(String.format("<p><i>%s</i></p>", message));

            out.write("<h2>Request 2:</h2>");
            long number;
            if (useSoapJMS)
                number = requestReplyWithSoapJms.getRandomNumber();
            else
                number = requestReplyWithSoapHttp.getRandomNumber();

            message = String.format("Generate a random number from server --> Response: %d", number);
            out.write(String.format("<p><i>%s</i></p>", message));
        } catch (Exception exc) {
            log.error("Error Occurred", exc);
            throw new ServletException(exc);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
