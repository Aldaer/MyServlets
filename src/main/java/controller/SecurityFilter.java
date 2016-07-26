package controller;

import lombok.extern.slf4j.Slf4j;
import model.dao.User;
import model.dao.UserDAO;

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
    private volatile long n = 0;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");

        final String uri = req.getRequestURI();
        log.trace("Filtering request {}: uri {}", ++n, uri);

        if (uri.equals(LOGOUT)) {
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
            switch(uri) {
                case MESSAGE_SERVLET:
                    res.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not logged in.");
                    return;
            }

            if (getServletContext().getAttribute("AUTOLOGIN") != null) {        // TODO: Remove in production!!!
                UserDAO userDao = (UserDAO) getServletContext().getAttribute(AttributeNames.C.USER_DAO);
                user = userDao.getUser((String) getServletContext().getAttribute("AUTOLOGIN"));
                log.warn("AUTOLOGIN = {}", user.getUsername());
                req.getSession(false).setAttribute(USER, user);
                req.getSession(false).setAttribute("language", "ru");
                req.getRequestDispatcher(MAIN_SERVLET).forward(req, res);
                return;
            }

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
        chain.doFilter(req, res);
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
