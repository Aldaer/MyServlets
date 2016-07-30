package controller;

import lombok.extern.slf4j.Slf4j;
import model.dao.Credentials;
import model.dao.CredentialsDAO;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import static controller.AttributeNames.C.CREDS_DAO;
import static controller.AttributeNames.R.REG_ATTEMPT;
import static controller.MiscConstants.*;
import static controller.PageURLs.*;
import static controller.ParameterNames.*;
import static java.util.Optional.ofNullable;

/**
 * Gets called from registration form in login.jsp
 */
@Slf4j
@WebServlet(REGISTER_SERVLET)
public class RegisterServlet extends HttpServlet {
    private static final Pattern validLogin = Pattern.compile("\\pL[\\-\\.\\w]{3,49}", Pattern.UNICODE_CHARACTER_CLASS);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("Processing request...");
        request.setCharacterEncoding("UTF-8");

        String newName = ofNullable(request.getParameter(USERNAME)).map(String::trim).orElse("");
        String newPassword = request.getParameter(PASSWORD);
        String newPassword2 = request.getParameter(PASSWORD2);

        // Re-check things checked by JavaScript client-side
        if (newPassword == null || newPassword2 == null ||
                newName.length() < MIN_USERNAME_LENGTH || newName.length() > MAX_USERNAME_LENGTH ||
                newPassword.length() < MIN_PASSWORD_LENGTH || ! newPassword.equals(newPassword2)) {
            log.debug("Invalid registration data for user '{}'", newName);
            returnToRegistration(request, response);
            return;
        }

        ServletContext srvContext = request.getServletContext();
        CredentialsDAO credsDao = (CredentialsDAO) srvContext.getAttribute(CREDS_DAO);

        if (credsDao.checkIfLoginOccupied(newName) || !userLoginValid(newName) || !credsDao.createTemporaryUser(newName)) {
            log.debug("Cannot create temporary registration for user '{}'", newName);
            request.setAttribute(REG_ATTEMPT, newName);
            returnToRegistration(request, response);
            return;
        }
        log.info("Created temporary registration for user '{}'", newName);
        Credentials newCreds = credsDao.storeNewCredentials(newName, newPassword);
        if (newCreds == null) {
            log.error("Error creating user {}", newName);
            request.setAttribute(REG_ATTEMPT, "");
            returnToRegistration(request, response);
            return;
        }
        log.info("Successfully created user '{}'", newName);
        request.getRequestDispatcher(LOGIN_SERVLET).forward(request, response);
    }

    static boolean userLoginValid(String login) {
        return validLogin.matcher(login).matches();
    }

    private void returnToRegistration(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
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
