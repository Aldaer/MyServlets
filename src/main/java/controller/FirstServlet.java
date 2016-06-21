package controller;

import model.MyTimer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

/**
 * My first attempt on servlets
 * Doubles as a JavaBean
 */
@WebServlet("serv")
public class FirstServlet extends HttpServlet {
    private static int id = 0;
    private MyTimer t;

    public FirstServlet() {
        t = new MyTimer("ru", "Europe/Moscow");
        ++id;
    }

    MyTimer getTimer() {
        return t;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (PrintWriter out = response.getWriter()) {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Time servlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>This is a time servlet</h1>");
            if (request.getSession().isNew()) out.println("Hello!");
            else out.println("Hello again!");
            String tz = Optional.ofNullable(request.getParameter("timezone")).orElse("GMT");
            if (!t.getTz().equals(tz)) {
                t = new MyTimer("en-us", tz);
                request.getSession().setAttribute("timer", t);
            }
            out.println("<br>" + t);
            out.println("<br>Servlet id: " + id);
            out.println("<br><a href=\"..\">Back...</a>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
