package com.softwareag.messaging;

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
 * A simple servlet 3 as client that sends several messages to a queue or a topic.
 * </p>
 * <p/>
 * <p/>
 * The servlet is registered and mapped to /SimpleMessageProducer using the {@linkplain javax.servlet.annotation.WebServlet
 *
 * @author Fabien
 * @HttpServlet}. </p>
 */
@WebServlet("/JcaMessageProducer")
public class JcaMessageProducer extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(JcaMessageProducer.class);

    private static final long serialVersionUID = -8314035702649252239L;

    private static final String MSG_TEXT = "Some text message";
    private static final int DEFAULT_MSG_COUNT = 5;

    //this uses the resource-adapter to make sure it's a managed connection etc...
    @Resource(mappedName = "java:/jms/broker")
    private ConnectionFactory connectionFactory;

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
            String destinationName = "";
            boolean isQueue;
            if (req.getParameterMap().keySet().contains("topic")) {
                isQueue = false;
                destinationName = req.getParameter("topic");
            } else {
                isQueue = true;
                destinationName = req.getParameter("queue");
            }

            if (null == destinationName || "".equals(destinationName)) {
                out.write("<p>Destination name not specified...can't do much.</p>");
                return;
            }

            int msgCount = 0;
            try {
                msgCount = Integer.parseInt(req.getParameter("count"));
            } catch (NumberFormatException e) {
                log.warn("default to " + DEFAULT_MSG_COUNT, e);
                msgCount = DEFAULT_MSG_COUNT;
            }

            if (log.isDebugEnabled())
                log.debug(String.format("Sending %d messages to %s [%s] using managed connection factory [%s]", msgCount, (isQueue) ? "queue" : "topic", destinationName, connectionFactory.getClass().getName()));
            out.write(String.format("<p>Sending %d messages to <em>%s [%s]</em>, <br />using managed connection factory [<em>%s</em>]</p>", msgCount, (isQueue) ? "queue" : "topic", destinationName, (null != connectionFactory) ? connectionFactory.getClass().getName() : "null"));

            for (int i = 0; i < msgCount; i++) {
                String message = "This is message " + (i + 1);
                sendMessage(message, destinationName, isQueue);
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
    private void sendMessage(String textToSend, String destinationName, boolean isQueue) throws JMSException {
        Connection connection = null;

        try {
            if (null == connectionFactory)
                throw new JMSException("connection factory is null...can't do anything.");

            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            //here we avoid a JNDI lookup...
            Destination destination;
            if (isQueue)
                destination = session.createQueue(destinationName);
            else
                destination = session.createTopic(destinationName);

            MessageProducer messageProducer = session.createProducer(destination);
            TextMessage message = session.createTextMessage();

            log.info(String.format("Sending new message to %s %s : %s ", (isQueue) ? "queue" : "topic", destinationName, textToSend));

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
