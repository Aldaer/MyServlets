package controller;

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
@WebServlet("/doLogin")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String userName = request.getParameter("username");
        String userPassword = request.getParameter("password");
        System.out.println("LOGGING IN USER = " + userName + " PASSWORD = " + userPassword);

        UserDAO userDAO = (UserDAO) this.getServletContext().getAttribute(USER_DAO);

        Optional<Long> id = userDAO.authenticateUser(userName, userPassword);
        if (id.isPresent()) {
            HttpSession s;
            if ((s = request.getSession(false)) != null) s.invalidate();                                      // Recreate session to combat session fixation attacks
            request.getSession(true).setAttribute(USER_ID, id.get());
            request.getSession(false).setAttribute(USER_NAME, userName);
            RequestDispatcher respJSP = request.getRequestDispatcher("/serv");
            respJSP.forward(request, response);
        } else {
            RequestDispatcher respLogin = request.getRequestDispatcher("login.jsp");
            respLogin.forward(request, response);
        }
    }

}
