package com.softwareag.messaging;

import com.softwareag.messaging.utils.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * <p>
 * A simplistic (on purpose) servlet that sends JMS messages without  JCA construct (no connection pooling and the likes...)
 * </p>
 *
 * @author Fabien Sanglier
 */
@WebServlet("/SimpleNoJCAJmsProducer")
public class SimpleNonJCAJmsProducer extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger log = LoggerFactory.getLogger(SimpleNonJCAJmsProducer.class);

    /**
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public SimpleNonJCAJmsProducer() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Testing");
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.write("<h1>Example demonstrates the use of <strong>JMS 1.1</strong> and <strong>EJB 3.1 Message-Driven Bean</strong> with ustom resource adapter in JBoss Enterprise Application 6 or JBoss AS 7.</h1>");
        try {
            String connectionUrl = AppConfig.getInstance().getPropertyHelper().getProperty("jms.connection.url");
            if (null == connectionUrl)
                throw new ServletException("jms.connection.url not defined.");

            String jndiConnectionFactory = AppConfig.getInstance().getPropertyHelper().getProperty("jms.connection.factory");
            if (null == jndiConnectionFactory)
                throw new ServletException("jms.connection.factory not defined.");

            boolean isQueue = true;
            String destinationType = AppConfig.getInstance().getPropertyHelper().getProperty("jms.destination.type", "queue");
            if ("topic".equalsIgnoreCase(destinationType)) {
                isQueue = false;
            } else if ("queue".equalsIgnoreCase(destinationType)) {
                isQueue = true;
            } else {
                throw new ServletException("jms.destination.type not valid.");
            }

            String destinationName = AppConfig.getInstance().getPropertyHelper().getProperty("jms.destination.name");
            if (null == destinationName)
                throw new ServletException("jms.destination.name not defined.");

            int msgTotal = -1;
            try {
                msgTotal = Integer.parseInt(req.getParameter("count"));
            } catch (NumberFormatException e) {
                msgTotal = -1;
            }

            if(msgTotal <= 0)
                msgTotal = AppConfig.getInstance().getPropertyHelper().getPropertyAsInt("default.message.count", 10);

            log.debug(String.format("Sending %d messages to url [%s], %s name [%s], using factory jndi name [%s]", msgTotal, connectionUrl, (isQueue) ? "queue" : "topic", destinationName, jndiConnectionFactory));
            out.write(String.format("<p>Sending %d messages to url [<em>%s</em>], <br /><em>%s [%s]</em>, <br />using factory jndi name [<em>%s</em>]</p>", msgTotal, connectionUrl, (isQueue) ? "queue" : "topic", destinationName, jndiConnectionFactory));

            int msgCount = 0;
            for (msgCount = 0; msgCount < msgTotal; msgCount++) {
                String message = "This is message " + (msgCount + 1);
                sendMessage(connectionUrl, jndiConnectionFactory, destinationName, message, isQueue);
                out.write("Message (" + msgCount + "): " + message + "</br>");
            }

            System.out.println(msgCount + " messages sent successfully");
            out.write("<p><i>" + msgCount + "messages sent successfully</i></p>");
        } catch (JMSException e) {
            log.error("Error occurred", e);
            out.write("<h2>A problem occurred during the delivery of this message: " + e.getMessage() + " </h2>");
            out.write("</br>");
            out.write("<p><i>Go your the JBoss Application Server console or Server log to see the error stack trace</i></p>");
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    private void sendMessage(String connectionUrl, String jndiConnectionFactory, String destinationName, String textToSend, boolean isQueue) throws JMSException {
        Connection connection = null;

        try {
            String contextFactory = AppConfig.getInstance().getPropertyHelper().getProperty("jms.connection.contextfactory");
            if (null == contextFactory)
                throw new ServletException("jms.connection.contextfactory not defined.");

            // creating properties file for getting initial context
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("java.naming.factory.initial", contextFactory);

            if (null != connectionUrl && !"".equals(connectionUrl)) {
                env.put("java.naming.provider.url", connectionUrl);
            }

            if (log.isDebugEnabled()) {
                for (String key : env.keySet()) {
                    log.debug(String.format("Context: %s - %s", key.toString(), env.get(key)));
                }
            }

            log.info(String.format("System Properties: %s - %s", "java.naming.provider.url", System.getProperty("java.naming.provider.url", "not-set!")));

            Context namingContext = new InitialContext(env);

            log.info("Context Created : " + namingContext.toString());

            // Lookup Connection Factory
            ConnectionFactory connectionFactory = (ConnectionFactory) namingContext.lookup(jndiConnectionFactory);

            log.info("Lookup Connection Factory Success : " + connectionFactory.toString());

            connection = connectionFactory.createConnection(); // Create connection

            log.info("Connection Created : " + connection.toString());

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE); // Create Session

            log.info("Session Created : " + connection.toString());

            //avoid another jndi lookup here
            Destination destination;
            if (isQueue)
                destination = session.createQueue(destinationName);
            else
                destination = session.createTopic(destinationName);

            log.info("Destination Created : " + destination);

            // Create Message Producer
            MessageProducer producer = session.createProducer(destination);

            // Create Message
            TextMessage msg = session.createTextMessage();
            msg.setText(textToSend);

            log.info(String.format("Sending new message to %s %s : %s ", (isQueue) ? "queue" : "topic", destinationName, textToSend));

            producer.send(msg); // Send Message

            log.info("message sent successfully");
        } catch (Exception e) {
            log.error("error while sending messages", e);
            throw new JMSException("Could not send messages");
        } finally {
            if (null != connection)
                connection.close();
        }
    }
}
