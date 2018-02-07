package com.softwareaggov.messaging.simplejmssendoneway.web;

import com.softwareaggov.messaging.libs.utils.AppConfig;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.publish.JmsPublisherLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 * Base Servlet
 * </p>
 * <p/>
 *
 * @author Fabien Sanglier
 * @HttpServlet}. </p>
 */
public abstract class BaseMessageProducer extends HttpServlet {
    private static final long serialVersionUID = -8314035702649252239L;

    private static final String MSG_TEXT = "Some text message";

    private static Logger log = LoggerFactory.getLogger(BaseMessageProducer.class);

    protected Random rdm;
    protected String messagePayload;
    protected Map<String, String> messageProperties;

    protected abstract JmsPublisherLocal getJmsPublisherLocal();

    @Override
    public void init() throws ServletException {
        super.init();

        rdm = new Random(System.currentTimeMillis());

        int payloadSize = AppConfig.getInstance().getPropertyHelper().getPropertyAsInt("jms.message.size", 512);
        byte[] chars = new byte[payloadSize];
        rdm.nextBytes(chars);
        messagePayload = new String(chars);

        HashMap props = new HashMap<String, String>(4);
        props.put("property1", new Integer(rdm.nextInt()).toString());
        props.put("property2", new Integer(rdm.nextInt()).toString());
        props.put("property3", new Integer(rdm.nextInt()).toString());
        props.put("property4", new Integer(rdm.nextInt()).toString());

        messageProperties = Collections.unmodifiableMap(props);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.write("<h1>" + req.getContextPath() + " - Sending JMS message To Queue</h1>");
        try {
            JmsPublisherLocal jmsPublisher = getJmsPublisherLocal();
            if (null == jmsPublisher)
                throw new IllegalArgumentException("jmsPublisher is null...should not be...check code or configs");

            if (jmsPublisher.isEnabled()) {
                String response = jmsPublisher.sendTextMessage(messagePayload, messageProperties);

                out.write("<p><b>messages sent successfully</b></p>");
                out.write(String.format("<div><p>Response:</p><p>%s</p></div>", ((null != response) ? response : "null")));
            } else {
                out.write("<p><b>Functionality not enabled by backend EJB!</b></p>");
            }

            out.close();
        } catch (Throwable exc) {
            log.error("Error Occurred", exc);
            throw new ServletException(exc);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
