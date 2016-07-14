package controller;

import lombok.extern.log4j.Log4j2;

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

import static controller.AttributeNames.USER_ID;
import static controller.AttributeNames.USER_NAME;

/**
 * My first attempt on HTTP Filters
 */
@Log4j2
@WebFilter(filterName = "Filter1st")
public class FirstRequestFilter extends HttpFilter {
    private int n = 0;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        log.trace("Filtering request {}", ++n);
        if ("logout".equals(req.getParameter("action"))) {
            String uname;
            if ((uname = (String) req.getSession().getAttribute(USER_NAME)) != null)
                log.info("Logging out user: {}", uname);
            req.getSession().invalidate();
            res.sendRedirect("index.jsp");
            return;
        }
        if (req.getSession().getAttribute(USER_ID) == null) {
            req.getRequestDispatcher("login.jsp").forward(req, res);
            return;
        }
        super.doFilter(req, res, chain);
        log.trace("Filtering response {}", n);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);

        log.debug("Initializing filter");
        log.debug("Filter name = " + config.getFilterName());
        log.debug("Servlet context = " + config.getServletContext().getContextPath());
        Collections.list(config.getInitParameterNames()).
                forEach(s -> log.debug("{} = {}", s, config.getInitParameter(s)));
    }
}
