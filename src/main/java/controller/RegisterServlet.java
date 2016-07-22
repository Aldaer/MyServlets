package controller;

import lombok.extern.slf4j.Slf4j;
import model.dao.CredentialsDAO;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static controller.AttributeNames.C.CREDS_DAO;
import static controller.AttributeNames.R.REG_ATTEMPT;
import static controller.MiscConstants.*;
import static controller.ParameterNames.L_PASSWORD;
import static controller.ParameterNames.L_USERNAME;

/**
 * Gets called from registration form in login.jsp
 */
@Slf4j
@WebServlet("/registerUser")
public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String newName = request.getParameter(L_USERNAME);
        String newPassword = request.getParameter(L_PASSWORD);
        String newPassword2 = request.getParameter(L_PASSWORD);

        // Usually checked by JavaScript client-side
        if (newName == null || newPassword == null || newPassword2 == null ||
                newName.length() < MIN_USERNAME_LENGTH || newName.length() > MAX_USERNAME_LENGTH ||
                newPassword.length() < MIN_PASSWORD_LENGTH || ! newPassword.equals(newPassword2)) {
            log.debug("Invalid registration data for user '{}'", newName);
            returnToRegistration(request, response);
            return;
        }

        ServletContext srvContext = request.getServletContext();
        CredentialsDAO credsDao = (CredentialsDAO) srvContext.getAttribute(CREDS_DAO);

        if (credsDao.checkIfUserExists(newName) || !credsDao.createTemporaryUser(newName)) {
            log.debug("Cannot create temporary registration for user '{}'", newName);
            request.setAttribute(REG_ATTEMPT, newName);
            returnToRegistration(request, response);
            return;
        }
        log.info("Created temporary registration for user '{}'", newName);



    }

    private void returnToRegistration(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/main/login.jsp").forward(request, response);
    }
}
