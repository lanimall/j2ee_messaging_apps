package com.softwareaggov.messaging.simplejmssendoneway.web;

import com.softwareaggov.messaging.libs.utils.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    public void destroy() {
        super.destroy();
    }
}
