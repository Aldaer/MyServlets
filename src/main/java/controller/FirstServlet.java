package controller;

import model.MyTimer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * My first attempt on servlets
 * Doubles as a JavaBean
 */
@WebServlet("/serv")
public class FirstServlet extends HttpServlet {
    private static int id = 0;
    private MyTimer t;

    public FirstServlet() { }

    MyTimer getTimer() {
        return t;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tz = Optional.ofNullable(request.getParameter("timezone")).orElse("GMT");
        if ((t == null) || !t.getTz().equals(tz)) {
            t = new MyTimer("en-us", tz);
            request.getSession().setAttribute("timer", t);
            request.setAttribute("lastTZ", tz);
        }
        RequestDispatcher respJSP = request.getRequestDispatcher("response.jsp");
        respJSP.forward(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
