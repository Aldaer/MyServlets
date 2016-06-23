package controller;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * My first attempt on HTTP Filters
 */
@WebFilter(servletNames = "controller.FirstServlet")
public class FirstRequestFilter extends HttpFilter {
    int n = 0;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        System.out.println("Filtering request #" + ++n);
        if (req.getSession().isNew()) System.out.println("New session detected");
        if ("restart".equals(req.getParameter("action"))) {
            req.getRequestDispatcher("index.jsp").forward(req, res);
            req.getSession().invalidate();
            return;
        }
        super.doFilter(req, res, chain);
        System.out.println("Filtering response #" + n);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);

        System.out.println("Initializing filter");
        System.out.println("Filter name = " + config.getFilterName());
        System.out.println("Servlet context = " + config.getServletContext().getContextPath());
        Collections.list(config.getInitParameterNames()).
                forEach(s -> System.out.println(s + " = " + config.getInitParameter(s)));
    }
}
