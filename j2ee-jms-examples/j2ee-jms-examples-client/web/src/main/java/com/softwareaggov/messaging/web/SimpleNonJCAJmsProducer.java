package com.softwareaggov.messaging.web;

import com.softwareaggov.messaging.libs.utils.AppConfig;
import com.softwareaggov.messaging.libs.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

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

    protected JMSHelper jmsHelper;
    protected Destination defaultDestination;

    @Override
    public void init() throws ServletException {
        super.init();

        //JNDI params
        String jndiContextFactory = AppConfig.getInstance().getPropertyHelper().getProperty("jms.jndi.contextfactory");
        if (null == jndiContextFactory)
            throw new IllegalArgumentException("jms.jndi.contextfactory not defined.");

        String jndiConnectionUrl = AppConfig.getInstance().getPropertyHelper().getProperty("jms.jndi.connection.url");
        if (null == jndiConnectionUrl)
            throw new IllegalArgumentException("jms.jndi.connection.url not defined.");

        Hashtable<String, String> jndiEnv = new Hashtable<String, String>();
        jndiEnv.put("java.naming.factory.initial", jndiContextFactory);

        if (null != jndiConnectionUrl && !"".equals(jndiConnectionUrl)) {
            jndiEnv.put("java.naming.provider.url", jndiConnectionUrl);
        }

        //JMS connection factory
        String jmsConnectionFactory = AppConfig.getInstance().getPropertyHelper().getProperty("jms.connection.factory");
        if (null == jmsConnectionFactory)
            throw new IllegalArgumentException("jms.connection.factory not defined.");

        try {
            jmsHelper = JMSHelper.createSender(jndiEnv, jmsConnectionFactory);
        } catch (NamingException e) {
            throw new ServletException(e);
        } catch (JMSException e) {
            throw new ServletException(e);
        }

        //JMS destination
        String destinationTypeName = AppConfig.getInstance().getPropertyHelper().getProperty("jms.destination.type", "queue");

        String destinationName = AppConfig.getInstance().getPropertyHelper().getProperty("jms.destination.name");
        if (null == destinationName)
            throw new ServletException("jms.destination.name not defined.");

        try {
            defaultDestination = jmsHelper.lookupDestination(destinationName, destinationTypeName);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.write("<h1>Sending JMS message</h1>");
        try {
            int randomNumber = rdm.nextInt();
            String message = String.format("This is a text message with random number: %d", randomNumber);

            Map<String, String> headerProperties = new HashMap<String, String>(4);
            headerProperties.put("number_property", new Integer(randomNumber).toString());

            Destination destinationLocal = defaultDestination;
            //we could allow setting up the destination from request parameters here...

            jmsHelper.sendTextMessage(destinationLocal, messagePayload, headerProperties);

            out.write(String.format("<p><i>%s</i></p>", message));
            out.write("<p><b>messages sent successfully</b></p>");
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
