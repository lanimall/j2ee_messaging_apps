package com.softwareag.messaging;

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
 * Servlet implementation class SimpleMessageProducer
 */
@WebServlet("/SimpleMessageProducer")
public class SimpleMessageProducer extends HttpServlet {
	private static Logger log = LoggerFactory.getLogger(SimpleMessageProducer.class);

	private static final long serialVersionUID = 1L;

	private static final int MSG_COUNT = 50;
	private static final String BROKER_CONTEXTFACTORY = "com.webmethods.jms.naming.WmJmsNamingCtxFactory";
    private static final String BROKER_URL = "wmjmsnaming://mybroker@wmvm:6849";
    private static final String BROKER_QUEUECONNECTIONFACTORY = "SimpleQueueConnectionFactory";
	private static final String BROKER_QUEUE = "simplequeue";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SimpleMessageProducer() {
		super();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("Testing");
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();

		out.write("<h1>Example demonstrates the use of <strong>JMS 1.1</strong> and <strong>EJB 3.1 Message-Driven Bean</strong> with ustom resource adapter in JBoss Enterprise Application 6 or JBoss AS 7.</h1>");
		try {
			String connectionUrl=req.getParameter("connectionUrl");
			if(null == connectionUrl)
                connectionUrl = BROKER_URL;

			String jndiConnectionFactory = req.getParameter("jndiConnectionFactory");
			String destinationName = "";
			boolean isQueue;
			if (req.getParameterMap().keySet().contains("topic")) {
				isQueue = false;
				destinationName = req.getParameter("topic");
			} else {
				isQueue = true;
				destinationName = req.getParameter("queue");
			}

			if(null == destinationName || "".equals(destinationName)){
				out.write("<p>Destination name not specified...can't do much.</p>");
				return;
			}

			log.debug(String.format("Sending %d messages to url [%s], %s name [%s], using factory jndi name [%s]",MSG_COUNT, connectionUrl, (isQueue)?"queue":"topic", destinationName, jndiConnectionFactory));

			out.write(String.format("<p>Sending %d messages to url [<em>%s</em>], <br /><em>%s [%s]</em>, <br />using factory jndi name [<em>%s</em>]</p>",MSG_COUNT, connectionUrl, (isQueue)?"queue":"topic", destinationName, jndiConnectionFactory));
			sendMessage(connectionUrl, jndiConnectionFactory, destinationName, isQueue);

			System.out.println("50 Messages Sent to " + "NIRVANA" + " Successfully");

			out.write("<p><i>Messages Sent Successfully</i></p>");
		} catch (JMSException e) {
            log.error("Error occurred", e);
			out.write("<h2>A problem occurred during the delivery of this message: " + e.getMessage() + " </h2>");
            out.write("</br>");
			out.write("<p><i>Go your the JBoss Application Server console or Server log to see the error stack trace</i></p>");
		} finally {
			if(out != null) {
				out.close();
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	private void sendMessage(String connectionUrl, String jndiConnectionFactory, String destinationName, boolean isQueue) throws JMSException {
		Connection connection = null;

		try
		{
			// creating properties file for getting initial context
			Hashtable<String,String> env = new Hashtable<String,String>();
			env.put("java.naming.factory.initial", BROKER_CONTEXTFACTORY);

            if(null != connectionUrl && !"".equals(connectionUrl)){
				env.put("java.naming.provider.url", connectionUrl);
				env.put("nirvana.provider.url", connectionUrl);
			}

			for(String key : env.keySet()){
				System.out.println(String.format("Context: %s - %s",key.toString(), env.get(key)));
			}

			System.out.println(String.format("System Properties: %s - %s","java.naming.provider.url", System.getProperty("java.naming.provider.url", "not-set!")));
			System.out.println(String.format("System Properties: %s - %s","nirvana.provider.url", System.getProperty("nirvana.provider.url", "not-set!")));
			
			Context namingContext = new InitialContext(env);

			System.out.println("Context Created : " + namingContext.toString());

			// Lookup Connection Factory
			ConnectionFactory connectionFactory = (ConnectionFactory) namingContext.lookup(jndiConnectionFactory);

			System.out.println("Lookup Connection Factory Success : " + connectionFactory.toString());

			connection = connectionFactory.createConnection(); // Create connection

			System.out.println("Connection Created : " + connection.toString());

			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE); // Create Session

			System.out.println("Session Created : " + connection.toString());

			//avoid another jndi lookup here
			Destination destination;
			if(isQueue)
				destination = session.createQueue(destinationName);
			else 
				destination = session.createTopic(destinationName);

			System.out.println("Destination Created : " + destination);

			// Create Message Producer
			MessageProducer producer = session.createProducer(destination); 

			// Create Message
			TextMessage msg = session.createTextMessage(); 

			for (int i = 0; i < MSG_COUNT; i++) {
				msg.setText("Im here for NIRVANA " + i);
				producer.send(msg); // Send Message
			}

			System.out.println(MSG_COUNT + " messages sent successfully");
		}
		catch (Exception e)
		{
			log.error("error while sending messages", e);
			throw new JMSException("Could not send messages");
		} finally {
			if(null != connection)
				connection.close();
		}
	}
}
