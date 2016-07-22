package controller;

import lombok.extern.slf4j.Slf4j;
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

import static controller.AttributeNames.C.LANGUAGE;
import static controller.MiscConstants.DEFAULT_LOCALE;
import static controller.PageURLs.MAIN_PAGE;
import static java.util.Optional.ofNullable;

/**
 * My first attempt on servlets
 */

@Slf4j
@WebServlet(name = "MainServlet", urlPatterns = "/main/serv")
public class MainServlet extends HttpServlet {
    private static int id = 0;

    public MainServlet() { }

    @SuppressWarnings("UnnecessaryReturnStatement")
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("Processing request...");
        String tz = ofNullable(request.getParameter("timezone")).orElse("GMT");
        String lang = ofNullable((String)request.getSession().getAttribute(LANGUAGE)).orElse(DEFAULT_LOCALE);
        MyTimer t = (MyTimer) request.getSession().getAttribute("timer");

        if (t == null || ! t.getTz().equals(tz)) {
            t = new MyTimer(lang, tz);
            request.getSession().setAttribute("timer", t);
        }
        request.setAttribute("lastTZ", tz);
        request.setAttribute("supportedTZ", new TimeZoneNames(lang).getSupportedTimeZones());
        RequestDispatcher respJSP = request.getRequestDispatcher(MAIN_PAGE);
        respJSP.forward(request, response);
        return;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ++id;
        log.info("Initializing servlet");
        log.info("Servlet id = {}", id);
        log.info("Servlet name = {}", config.getServletName());
    }
}
