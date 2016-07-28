package controller;

import lombok.extern.slf4j.Slf4j;
import model.MyTimer;
import model.dao.MessageDAO;
import model.dao.User;
import model.dao.UserDAO;
import model.utils.TimeZoneNames;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;

import static controller.AttributeNames.*;
import static controller.MiscConstants.DEFAULT_LOCALE;
import static controller.MiscConstants.UNREAD_PRIVATE;
import static controller.PageURLs.*;
import static java.util.Optional.ofNullable;

/**
 * My first attempt on servlets
 */

@Slf4j
@WebServlet(name = "MainServlet", urlPatterns = {MAIN_SERVLET, USER_UPDATE_SERVLET})
public class MainServlet extends HttpServlet {
    @SuppressWarnings("UnnecessaryReturnStatement")
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        log.debug("Processing request...");

        switch (request.getRequestURI()) {
            case USER_UPDATE_SERVLET:
                processUserUpdate(request, response);
                break;
            case MAIN_SERVLET:
            default:
                processMainRequest(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
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


    @SuppressWarnings("UnnecessaryReturnStatement")
    private void processMainRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String tz = ofNullable(request.getParameter("timezone")).orElse("GMT");
        String lang = ofNullable((String) session.getAttribute(C.LANGUAGE)).orElse(DEFAULT_LOCALE);
        MyTimer t = (MyTimer) session.getAttribute("timer");

        if (t == null || !t.getTz().equals(tz)) {
            t = new MyTimer(lang, tz);
            session.setAttribute("timer", t);
        }
        request.setAttribute("lastTZ", tz);
        request.setAttribute("supportedTZ", new TimeZoneNames(lang).getSupportedTimeZones());

        User user = (User) session.getAttribute(S.USER);
        MessageDAO mDao = (MessageDAO) getServletContext().getAttribute(C.MSG_DAO);

        MessageDAO.MessageFilter privateUnread = MessageDAO.MessageFilter.newBuilder()
                .setTo(user.getUsername())
                .setConvId(UNREAD_PRIVATE);
        Integer unreadPrivateMessages = mDao.countMessages(privateUnread);
        request.setAttribute(R.UNREAD_PM, unreadPrivateMessages);

        RequestDispatcher respJSP = request.getRequestDispatcher(MAIN_PAGE);
        respJSP.forward(request, response);
        return;
    }

    private void processUserUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(S.USER);
        String newName = request.getParameter(ParameterNames.U.FULLNAME);
        String newEmail = request.getParameter(ParameterNames.U.EMAIL);
        if (newName == null || newName.equals("")) newName = user.getUsername();
        if (newEmail == null) newEmail = "";
        user.setFullName(newName);
        user.setEmail(newEmail);
        user.setRegComplete(true);
        UserDAO userDAO = (UserDAO) getServletContext().getAttribute(C.USER_DAO);
        log.info("Updating user info for user '{}'", user.getUsername());
        userDAO.updateUserInfo(user);
        response.sendRedirect(DETAILS_PAGE);
    }

}
