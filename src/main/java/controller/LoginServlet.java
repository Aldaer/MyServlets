package controller;

import lombok.extern.slf4j.Slf4j;
import model.dao.Credentials;
import model.dao.CredentialsDAO;
import model.dao.User;
import model.dao.UserDAO;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;

import static controller.AttributeNames.*;
import static controller.MiscConstants.DEFAULT_LOCALE;
import static controller.PageURLs.*;
import static controller.ParameterNames.*;

/**
 * Login servlet. Accepts only POST requests
 */
@Slf4j
@WebServlet(LOGIN_SERVLET)
/**
 * Gets called EITHER from login form (when container-based authentication is off)
 * or by forward from Security filter (when container-based authentication is on)
 */
public class LoginServlet extends HttpServlet {
    @SuppressWarnings("UnnecessaryReturnStatement")
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        Principal authUser = request.getUserPrincipal();
        final ServletContext srvContext = getServletContext();
        UserDAO userDao = (UserDAO) srvContext.getAttribute(C.USER_DAO);

        String login;
        if (authUser == null) {                     // Not authenticated by container
            CredentialsDAO credsDao = (CredentialsDAO) srvContext.getAttribute(C.CREDS_DAO);

            String userName = request.getParameter(USERNAME);
            String userPassword = request.getParameter(PASSWORD);

            Credentials creds = credsDao.getCredentials(userName);
            if (creds == null) {
                log.debug("User '{}' not found", userName);
                request.setAttribute(R.USER_FOUND, Boolean.FALSE);
                RequestDispatcher respLogin = request.getRequestDispatcher(LOGIN_PAGE);
                respLogin.forward(request, response);
                return;
            } else if (!creds.verify(userPassword)) {
                log.info("User '{}' presented wrong password", userName);
                request.setAttribute(R.USER_FOUND, Boolean.TRUE);
                RequestDispatcher respLogin = request.getRequestDispatcher(LOGIN_PAGE);
                respLogin.forward(request, response);
                return;
            }
            log.info("Logging in user = '{}', password = *HIDDEN*", userName);
            login = userName;
        } else {                                    // Authenticated by container
            login = authUser.getName();
            log.debug("Logging in user = '{}', auth = container", login);
        }

        // Recreate session to combat session fixation attacks. ONLY if container security is OFF.
        if (! (Boolean)srvContext.getAttribute(C.CONTAINER_AUTH) && request.getSession(false) != null)
            request.getSession().invalidate();

        final User user = userDao.getUser(login);
        assert user != null;
        request.getSession(true).setAttribute(S.USER, user);

        String lang = request.getParameter(ParameterNames.LANGUAGE);
        if (lang == null || lang.equals("")) lang = DEFAULT_LOCALE;
        request.getSession(false).setAttribute(LANGUAGE, lang);

        String whereTo = user.isRegComplete()? MAIN_SERVLET : DETAILS_PAGE;
        if (request.getDispatcherType() == DispatcherType.FORWARD)
            request.getRequestDispatcher(whereTo).forward(request, response);
        else
            response.sendRedirect(whereTo);
        return;
    }

    /**
     * Only valid if called by a forwarded request from security filter
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getUserPrincipal() != null) {            // User already authenticated by the container
            doPost(request, response);
            return;
        }

        // Won't accept user credentials if sent through GET method, redirect without authentication will bring user to login page
        response.sendRedirect(MAIN_PAGE);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        log.info("Initializing servlet: name = {}, mappings = {}",
                config.getServletName(),
                Arrays.toString(getServletContext()
                        .getServletRegistration(config.getServletName())
                        .getMappings().stream().toArray(String[]::new)));
    }
}