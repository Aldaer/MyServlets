package controller;

import lombok.extern.slf4j.Slf4j;
import model.dao.User;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static controller.AttributeNames.USER;

/**
 * Login filter - to be replaced with container-based auth
 */
@Slf4j
@WebFilter(filterName = "SecurityFilter")
public class FirstRequestFilter extends HttpFilter {
    private int n = 0;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        log.trace("Filtering request {}", ++n);
        if ("logout".equals(req.getParameter("action"))) {
            final Optional<String> uname = Optional.ofNullable(req.getSession(false)).map(sn -> (User) sn.getAttribute(USER)).map(User::getUsername);
            if (uname.isPresent())
                log.info("Logging out user: {}", uname.get());
            req.getSession().invalidate();
            res.sendRedirect("/index.html");
            return;
        }
        if (req.getSession().getAttribute(USER) == null) {
            req.getRequestDispatcher("/login.jsp").forward(req, res);
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
