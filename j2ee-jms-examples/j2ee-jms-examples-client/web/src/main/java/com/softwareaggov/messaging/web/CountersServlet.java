package com.softwareaggov.messaging.web;

import com.softwareaggov.messaging.service.utils.CounterSingletonLocal;
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
 * <p>
 * The servlet is registered and mapped to /JcaQueueProxyMessageProducer using the {@linkplain javax.servlet.annotation.WebServlet
 * @author Fabien Sanglier
 * @HttpServlet}. </p>
 */
@WebServlet("/messagecounters")
public class CountersServlet extends BaseMessageProducer {
    private static final long serialVersionUID = -8314702649252239L;
    private static Logger log = LoggerFactory.getLogger(CountersServlet.class);

    @EJB
    private CounterSingletonLocal messageProcessingCounter;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        String counterName = req.getParameter("countername");
        String reset = req.getParameter("reset");

        String[] counterNames;
        if(null != counterName && !"".equals(counterName)) {
            out.write("<h1>Printing specific counters</h1>");
            counterNames = new String[] {counterName};
        } else {
            out.write("<h1>Printing all counters</h1>");
            counterNames = messageProcessingCounter.getAllCounterNames();
        }

        try {
            out.write("<ul>");
            for(String counterKey : counterNames){
                if (null != reset && "true".equalsIgnoreCase(reset))
                    messageProcessingCounter.reset(counterKey);

                out.write(String.format("<li>Counter [%s] = %d (Rate= %d / sec)</li>", counterKey, messageProcessingCounter.getCount(counterKey), messageProcessingCounter.getCountRate(counterKey)));
            }
            out.write("</ul>");
        } catch (Exception exc){
            log.error("Error Occurred", exc);
            throw new ServletException(exc);
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
