package controller;

import model.MyTimer;
import model.utils.TimeZoneNames;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * My first attempt on servlets
 * Doubles as a JavaBean
 */

@WebServlet("/serv")
public class FirstServlet extends HttpServlet {
    private static int id = 0;

    public FirstServlet() { }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Processing request...");
        String tz = Optional.ofNullable(request.getParameter("timezone")).orElse("GMT");
        String lang = Optional.ofNullable((String)request.getSession().getAttribute("language")).orElse("en");
        MyTimer t = (MyTimer) request.getSession().getAttribute("timer");

        if (t == null || ! t.getTz().equals(tz)) {
            System.out.println("Creating new timer");
            t = new MyTimer(lang, tz);
            request.getSession().setAttribute("timer", t);
        }
        request.setAttribute("lastTZ", tz);
        request.setAttribute("supportedTZ", new TimeZoneNames(lang).getSupportedTimeZones());
        RequestDispatcher respJSP = request.getRequestDispatcher("response.jsp");
        respJSP.forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ++id;
        System.out.println("Initializing servlet");
        System.out.println("Servlet id = " + id);
        System.out.println("Servlet name = " + config.getServletName());
        System.out.println("Servlet context = " + config.getServletContext().getContextPath());
        Collections.list(config.getInitParameterNames()).forEach(s -> System.out.println(s + " = " + config.getInitParameter(s)));
    }
}
