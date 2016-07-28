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
import static controller.MiscConstants.*;
import static controller.PageURLs.*;
import static java.util.Optional.ofNullable;

/**
 * My first attempt on servlets
 */

@Slf4j
@WebServlet(name = "MainServlet", urlPatterns = {MAIN_SERVLET, USER_UPDATE_SERVLET, USER_SEARCH_SERVLET})
public class MainServlet extends HttpServlet {
    @SuppressWarnings("UnnecessaryReturnStatement")
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        log.debug("Processing request...");

        switch (req.getRequestURI()) {
            case USER_UPDATE_SERVLET:
                processUserUpdate(req, res);
                break;
            case USER_SEARCH_SERVLET:
                processUserSearch(req, res);
                break;
            case MAIN_SERVLET:
            default:
                processMainRequest(req, res);
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doPost(req, res);
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
    private void processMainRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String tz = ofNullable(req.getParameter("timezone")).orElse("GMT");
        String lang = ofNullable((String) session.getAttribute(C.LANGUAGE)).orElse(DEFAULT_LOCALE);
        MyTimer t = (MyTimer) session.getAttribute("timer");

        if (t == null || !t.getTz().equals(tz)) {
            t = new MyTimer(lang, tz);
            session.setAttribute("timer", t);
        }
        req.setAttribute("lastTZ", tz);
        req.setAttribute("supportedTZ", new TimeZoneNames(lang).getSupportedTimeZones());

        User user = (User) session.getAttribute(S.USER);
        MessageDAO mDao = (MessageDAO) getServletContext().getAttribute(C.MSG_DAO);

        MessageDAO.MessageFilter privateUnread = MessageDAO.MessageFilter.newBuilder()
                .setTo(user.getUsername())
                .setConvId(UNREAD_PRIVATE);
        Integer unreadPrivateMessages = mDao.countMessages(privateUnread);
        req.setAttribute(R.UNREAD_PM, unreadPrivateMessages);

        RequestDispatcher respJSP = req.getRequestDispatcher(MAIN_PAGE);
        respJSP.forward(req, res);
        return;
    }

    private void processUserUpdate(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute(S.USER);
        String newName = req.getParameter(ParameterNames.U.FULLNAME);
        String newEmail = req.getParameter(ParameterNames.U.EMAIL);
        if (newName == null || newName.equals("")) newName = user.getUsername();
        if (newEmail == null) newEmail = "";
        user.setFullName(newName);
        user.setEmail(newEmail);
        user.setRegComplete(true);
        UserDAO userDAO = (UserDAO) getServletContext().getAttribute(C.USER_DAO);
        log.info("Updating user info for user '{}'", user.getUsername());
        userDAO.updateUserInfo(user);
        res.sendRedirect(DETAILS_PAGE);
    }


    private void processUserSearch(HttpServletRequest req, HttpServletResponse res) {
        log.debug("Processing search request: {}", req.getParameter("query"));

        res.setContentType(JSON_TYPE);

    }

}
