package controller;

import lombok.extern.slf4j.Slf4j;
import model.dao.Credentials;
import model.dao.CredentialsDAO;
import model.dao.User;
import model.dao.UserDAO;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

import static controller.ContextAttributeNames.*;
import static controller.MiscConstants.DEFAULT_LOCALE;
import static controller.PageURLs.LOGIN_PAGE;
import static controller.PageURLs.MAIN_SERVLET;

/**
 * Login servlet. Accepts only POST requests
 */
@Slf4j
@WebServlet("/doLogin")
/**
 * Gets called EITHER from login form (when container-based authentication is off)
 * or from forward by Security filter (when container-based authentication is on)
 */
public class LoginServlet extends HttpServlet {
    @SuppressWarnings("UnnecessaryReturnStatement")
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Principal authUser = request.getUserPrincipal();
        UserDAO userDao = (UserDAO) getServletContext().getAttribute(USER_DAO);

        String login;
        if (authUser == null) {                     // Not authenticated by container
            CredentialsDAO credsDao = (CredentialsDAO) getServletContext().getAttribute(CREDS_DAO);

            request.setCharacterEncoding("UTF-8");
            String userName = request.getParameter("j_username");
            String userPassword = request.getParameter("j_password");

            Credentials creds = credsDao.getCredentials(userName);
            if (creds.verify(userPassword)) {
                log.info("USER = {}: LOGIN FAILED", userName);
                RequestDispatcher respLogin = request.getRequestDispatcher(LOGIN_PAGE);
                respLogin.forward(request, response);
                return;
            }
            log.info("LOGGING IN USER = {}, PASSWORD = *HIDDEN*", userName);
            login = userName;
        } else {                                    // Authenticated by container
            login = authUser.getName();
        }

        // Recreate session to combat session fixation attacks. ONLY if container security is OFF.
        if (! "true".equals(request.getServletContext().getAttribute(CONTAINER_AUTH)) && (request.getSession(false) != null))
            request.getSession().invalidate();

        final User user = userDao.getUser(login);
        assert user != null;
        request.getSession(true).setAttribute(USER, user);

        String lang = request.getParameter(LANGUAGE);
        if (lang == null || lang.equals("")) lang = DEFAULT_LOCALE;
        request.getSession(false).setAttribute(LANGUAGE, lang);
        request.getRequestDispatcher(MAIN_SERVLET).forward(request, response);
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

        response.sendRedirect(LOGIN_PAGE);                  // Won't accept user credentials if sent through GET method
    }
}