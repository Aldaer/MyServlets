package controller;

import lombok.extern.slf4j.Slf4j;
import model.dao.User;
import model.dao.UserDAO;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static controller.AttributeNames.*;

/**
 * Login servlet. Accepts only POST requests
 */
@Slf4j
@WebServlet("/doLogin")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String userName = request.getParameter("username");
        String userPassword = request.getParameter("password");

        UserDAO userDAO = (UserDAO) getServletContext().getAttribute(USER_DAO);

        User uid = userDAO.getUser(userName);
        if (userDAO.authenticateUser(uid, userPassword)) {
            assert uid != null;
            log.info("LOGGING IN USER = {}, PASSWORD = *HIDDEN*", userName);

            HttpSession s;
            if ((s = request.getSession(false)) != null) s.invalidate();                                      // Recreate session to combat session fixation attacks
            request.getSession(true).setAttribute(USER_ID, uid.getId());
            request.getSession(false).setAttribute(USER_NAME, userName);
            request.getSession(false).setAttribute(LANGUAGE, request.getParameter(LANGUAGE));
            response.sendRedirect(request.getServletContext().getContextPath() + "/serv");
        } else {
            log.info("USER = {}: LOGIN FAILED", userName);
            RequestDispatcher respLogin = request.getRequestDispatcher("login.jsp");
            respLogin.forward(request, response);
        }
    }

}
