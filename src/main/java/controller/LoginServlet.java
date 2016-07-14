package controller;

import lombok.extern.log4j.Log4j2;
import model.dao.UserDAO;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

import static controller.AttributeNames.*;

/**
 * Login servlet. Accepts only POST requests
 */
@Log4j2
@WebServlet("/doLogin")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String userName = request.getParameter("username");
        String userPassword = request.getParameter("password");

        UserDAO userDAO = (UserDAO) this.getServletContext().getAttribute(USER_DAO);

        Optional<Long> id = userDAO.authenticatedId(userName, userPassword);
        if (id.isPresent()) {
            log.info("LOGGING IN USER = {}, PASSWORD = *HIDDEN*", userName);

            HttpSession s;
            if ((s = request.getSession(false)) != null) s.invalidate();                                      // Recreate session to combat session fixation attacks
            request.getSession(true).setAttribute(USER_ID, id.get());
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
