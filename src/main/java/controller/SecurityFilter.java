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
import java.util.Arrays;

import static controller.AttributeNames.S.USER;
import static controller.PageURLs.*;
import static java.util.Optional.ofNullable;

/**
 * Login filter - to be replaced with container-based auth
 */
@Slf4j
@WebFilter(urlPatterns = SECURED_AREA)
public class SecurityFilter extends HttpFilter {
    private int n = 0;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        log.trace("Filtering request {}: uri {}", ++n, req.getRequestURI());

        if ("logout".equals(req.getParameter(ParameterNames.ACTION))) {
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

            res.sendRedirect("/");
            return;
        }
        User user = (User) req.getSession(true).getAttribute(USER);
        if (user == null) {
            final Principal authUser = req.getUserPrincipal();          // User already authenticated by container?
            if (authUser == null) {
                log.trace("No user info, forwarding to login page");
                req.getRequestDispatcher(LOGIN_PAGE).forward(req, res);
                return;
            }

            log.info("User {} authenticated by app container", authUser.getName());
            req.getRequestDispatcher(LOGIN_SERVLET).forward(req, res);
            return;
        }
        if (! user.isRegComplete())                     // Registration incomplete, forward to user details page (once per session)
            req.getRequestDispatcher(DETAILS_PAGE).forward(req, res);
        else
            chain.doFilter(req, res);
        log.trace("[SEC] Filtering response {}", n);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);

        log.info("Initializing filter: name = {}, mappings = {}",
                config.getFilterName(),
                Arrays.toString(getServletContext()
                        .getFilterRegistration(config.getFilterName())
                        .getUrlPatternMappings()
                        .stream().toArray(String[]::new)));
    }
}
