package com.softwareag.messaging;

import com.softwareag.messaging.utils.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jms.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

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
@WebServlet("/JcaQueueProxyMessageProducer")
public class JcaQueueProxyMessageProducer extends HttpServlet {
    private static final long serialVersionUID = -8314702649252239L;
    private static final int DEFAULT_MSG_COUNT = 5;
    private static Logger log = LoggerFactory.getLogger(JcaQueueProxyMessageProducer.class);

    //this uses the resource-adapter to make sure it's a managed connection etc...
    @Resource(mappedName = "java:/jms/broker2")
    private ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/jms/SomeSimpleQueue")
    private Destination jmsDestination;

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.write("<h1>Example demonstrates the use of <strong>JMS 1.1</strong> coupled with custom Resource Adapter</h1>");

        try {
            int msgTotal = -1;
            try {
                msgTotal = Integer.parseInt(req.getParameter("count"));
            } catch (NumberFormatException e) {
                msgTotal = -1;
            }

            if(msgTotal <= 0)
                msgTotal = AppConfig.getInstance().getPropertyHelper().getPropertyAsInt("default.message.count", 10);

            if (log.isDebugEnabled())
                log.debug(String.format("Sending %d messages using managed object [%s]", msgTotal, (null != jmsDestination) ? jmsDestination.toString() : "null"));
            out.write(String.format("<p>Sending %d messages using managed object [%s]</p>", msgTotal, (null != jmsDestination) ? jmsDestination.toString() : "null"));

            for (int i = 0; i < msgTotal; i++) {
                String message = "This is message " + (i + 1);
                sendMessage(message);
                out.write("Message (" + i + "): " + message + "</br>");
            }

            out.write("<p><i>Messages Sent Successfully</i></p>");
        } catch (JMSException e) {
            log.error("Error occurred", e);
            out.write("<h2>A problem occurred during the delivery of this message</h2>");
            out.write("</br>");
            out.write("<p><i>Go your the JBoss Application Server console or Server log to see the error stack trace</i></p>");
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /*
     * The connection factory here is a managed one...and as such, open/close connections is all managed by the resource adapter which should use pooling instead...
     */
    private void sendMessage(String textToSend) throws JMSException {
        Connection connection = null;

        try {
            if (null == connectionFactory)
                throw new JMSException("connection factory is null...can't do anything.");

            connection = connectionFactory.createConnection();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageProducer messageProducer = session.createProducer(jmsDestination);

            TextMessage message = session.createTextMessage();

            log.info(String.format("Sending new message to managed object %s : %s ", (null != jmsDestination) ? jmsDestination.toString() : "null", textToSend));

            message.setText(textToSend);
            messageProducer.send(message); // Send Message

            log.info(String.format("Messages Sent"));
        } catch (Exception e) {
            log.error("error while sending message", e);
            throw new JMSException("Couldn't send to queue");
        } finally {
            if (null != connection)
                connection.close();
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
