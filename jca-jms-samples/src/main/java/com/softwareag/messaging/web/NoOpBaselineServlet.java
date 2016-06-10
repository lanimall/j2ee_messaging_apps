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
import javax.servlet.http.HttpServlet;
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
@WebServlet("/NoOpMessagePrint")
public class NoOpBaselineServlet extends HttpServlet {
    private static final long serialVersionUID = -8314702649252239L;
    private static Logger log = LoggerFactory.getLogger(NoOpBaselineServlet.class);

    protected Random rdm;

    @Override
    public void init() throws ServletException {
        super.init();
        this.rdm = new Random(System.currentTimeMillis());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        try {
            out.write("<h1>Printing message</h1>");
            String message = String.format("This is a text message with random number: %d", rdm.nextInt());
            out.write(String.format("<p><i>%s</i></p>", message));
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
