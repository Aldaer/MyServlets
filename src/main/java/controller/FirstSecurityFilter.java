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
import java.security.Principal;
import java.util.Collections;

import static controller.ContextAttributeNames.USER;
import static controller.PageURLs.LOGIN_PAGE;
import static java.util.Optional.ofNullable;

/**
 * Login filter - to be replaced with container-based auth
 */
@Slf4j
@WebFilter(filterName = "SecurityFilter")
public class FirstSecurityFilter extends HttpFilter {
    private int n = 0;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        log.trace("[SEC] Filtering request {}: {}", ++n, req.getRequestURL());

        if ("logout".equals(req.getParameter("action"))) {
            if (req.getUserPrincipal() != null) try {
                req.logout();
            } catch (ServletException e) {
                log.warn("Logout error: {}", e);
            }
            ofNullable(req.getSession(false)).ifPresent(sn -> {
                ofNullable(sn.getAttribute(USER))
                        .map(u -> (User) u)
                        .map(User::getUsername)
                        .ifPresent(un -> log.info("Logging out user: {}", un));
                sn.invalidate();
            });

            res.sendRedirect("/index.html");
            return;
        }
        if (req.getSession().getAttribute(USER) == null) {
            final Principal authUser = req.getUserPrincipal();          // User already authenticated by container?
            if (authUser == null) {
                log.trace("No user info, forwarding to login page");
                req.getRequestDispatcher(LOGIN_PAGE).forward(req, res);
                return;
            }

            log.info("User {} authenticated by app container", authUser.getName());
            req.getRequestDispatcher("/doLogin").forward(req, res);
            return;
        }
        super.doFilter(req, res, chain);
        log.trace("[SEC] Filtering response {}", n);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);

        log.debug("Initializing filter");
        log.debug("Filter name = " + config.getFilterName());
        log.debug("Servlet context path = " + config.getServletContext().getContextPath());
        Collections.list(config.getInitParameterNames()).
                forEach(s -> log.debug("{} = {}", s, config.getInitParameter(s)));
    }
}
