package com.softwareaggov.messaging.simplejmssendoneway.web;

import com.softwareaggov.messaging.simplejmssendoneway.ejb.utils.CounterLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/*
 * @author Fabien Sanglier
 *
 */
@WebServlet("/messagecounters")
public class CountersServlet extends HttpServlet {
    private static final long serialVersionUID = -8314702649252239L;
    private static Logger log = LoggerFactory.getLogger(CountersServlet.class);

    @EJB
    private CounterLocal messageProcessingCounter;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        String counterName = req.getParameter("countername");
        String resetParam = req.getParameter("reset");

        boolean reset = (null != resetParam && "true".equalsIgnoreCase(resetParam)) ? true : false;

        String[] counterNames;
        if (null != counterName && !"".equals(counterName)) {
            out.write("<h1>" + req.getContextPath() + " - Printing specific counters</h1>");
            counterNames = new String[]{counterName};
        } else {
            out.write("<h1>" + req.getContextPath() + " - Printing all counters</h1>");
            counterNames = messageProcessingCounter.getAllCounterNames();
        }

        try {
            out.write("<a href=" + req.getRequestURI() + "?reset=true" + ">Reset All Usage Counters</a>");
            out.write("<ul>");
            for (String counterKey : counterNames) {
                if (reset) messageProcessingCounter.reset(counterKey);
                out.write(String.format("<li>Counter [%s] = %d (Rate= %d / sec)</li>", counterKey, messageProcessingCounter.getCount(counterKey), messageProcessingCounter.getCountRate(counterKey)));
            }
            out.write("</ul>");

            //if reset, redirect to same page without the param
            if (reset) resp.sendRedirect(req.getRequestURI());
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
