package controller;

import lombok.extern.slf4j.Slf4j;
import model.MyTimer;
import model.dao.Message;
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
import static controller.utils.MyStringUtils.parseOrDefault;
import static controller.utils.MyStringUtils.parseOrNull;
import static java.util.Optional.ofNullable;

/**
 * My first attempt on servlets
 */

@Slf4j
@WebServlet(name = "MainServlet", urlPatterns = {MAIN_SERVLET, USER_UPDATE_SERVLET, MESSAGE_ACTION_SERVLET})
public class MainServlet extends HttpServlet {
    @SuppressWarnings("UnnecessaryReturnStatement")
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        log.debug("Processing request...");

        switch (req.getRequestURI()) {
            case USER_UPDATE_SERVLET:
                processUserUpdate(req, res);
                return;
            case MESSAGE_ACTION_SERVLET:
                switch(req.getParameter("action")) {
                    case "update":
                        processMessageUpdate(req, res);
                        return;
                    case "send":
                        processMessageSend(req, res);
                        return;
                }
                return;
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
        String newFullName = req.getParameter("fullname");
        String newEmail = req.getParameter("email");
        if (newFullName == null || newFullName.equals("")) newFullName = user.getUsername();
        if (newEmail == null) newEmail = "";
        user.setFullName(newFullName);
        user.setEmail(newEmail);
        user.setRegComplete(true);
        UserDAO userDAO = (UserDAO) getServletContext().getAttribute(C.USER_DAO);
        log.info("Updating user info for user '{}'", user.getUsername());
        userDAO.updateUserInfo(user);
        res.sendRedirect(DETAILS_PAGE);
    }

    private void processMessageUpdate(HttpServletRequest req, HttpServletResponse res) {
        Long id = parseOrNull(req.getParameter("id"));
        if (id == null) return;
        Boolean unread = ofNullable(req.getParameter("unread")).map(String::toLowerCase).map("true"::equals).orElse(null);
        String newText = req.getParameter("newText");

        MessageDAO mDao = (MessageDAO) getServletContext().getAttribute(C.MSG_DAO);
        // Null parameters are ignored
        mDao.updateMessage(id, newText, unread);            // TODO: message update authorization, update timestamp
    }

    private void processMessageSend(HttpServletRequest req, HttpServletResponse res) {
        MessageDAO mDao = (MessageDAO) getServletContext().getAttribute(C.MSG_DAO);
        User user = (User) req.getSession().getAttribute(S.USER);

        long refId = parseOrDefault(req.getParameter("refId"), 0);
        long convId = parseOrDefault(req.getParameter("convId"), 0);
        Message newMsg = new Message(0,
                refId,
                user.getUsername(),
                req.getParameter("to"),
                null,
                convId,
                req.getParameter("text"));
        mDao.sendMessage(newMsg);
    }

}