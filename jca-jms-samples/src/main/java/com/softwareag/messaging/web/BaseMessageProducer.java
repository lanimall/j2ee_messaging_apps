package com.softwareag.messaging.web;

import com.softwareag.messaging.utils.AppConfig;
import com.softwareag.messaging.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 * Base Servlet
 * </p>
 * <p>
 *
 * @author Fabien Sanglier
 * @HttpServlet}. </p>
 */
public abstract class BaseMessageProducer extends HttpServlet {
    private static final long serialVersionUID = -8314035702649252239L;

    private static final String MSG_TEXT = "Some text message";

    private static Logger log = LoggerFactory.getLogger(BaseMessageProducer.class);

    protected JMSHelper jmsHelper;

    protected Random rdm;

    protected String messagePayload;

    @Override
    public void init() throws ServletException {
        super.init();
        this.jmsHelper = createMessageSender();
        this.rdm = new Random(System.currentTimeMillis());

        int messageSize = AppConfig.getInstance().getPropertyHelper().getPropertyAsInt("jms.message.size", 512);
        byte[] chars = new byte[messageSize];
        rdm.nextBytes(chars);
        this.messagePayload = new String(chars);
    }

    protected abstract JMSHelper createMessageSender() throws ServletException;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.write("<h1>Sending JMS message</h1>");
        try {
            String message = String.format("This is a text message with random number: %d", rdm.nextInt());

            Map<String,String> payload = new HashMap<String, String>(1);
            payload.put(JMSHelper.PAYLOAD_TEXTMSG_PROPERTY, message);
            payload.put(JMSHelper.PAYLOAD_BYTES_PROPERTY, messagePayload);

            jmsHelper.sendMessage(payload);
            out.write(String.format("<p><i>%s</i></p>", message));
            out.write("<p><b>messages sent successfully</b></p>");
        } catch (JMSException e) {
            log.error("Error occurred", e);
            throw new ServletException(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
