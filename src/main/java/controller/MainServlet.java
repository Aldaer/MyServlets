package controller;

import lombok.extern.slf4j.Slf4j;
import model.MyTimer;
import model.dao.*;
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
import java.util.Collection;
import java.util.stream.Collectors;

import static controller.AttributeNames.*;
import static controller.MiscConstants.DEFAULT_LOCALE;
import static controller.MiscConstants.UNREAD_PRIVATE;
import static controller.PageURLs.*;
import static controller.utils.MyStringUtils.parseOrDefault;
import static controller.utils.MyStringUtils.parseOrNull;
import static java.util.Optional.ofNullable;

/**
 * Main servlet, processing user data and messaging
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
                switch (req.getParameter("action")) {
                    case "update":
                        processMessageUpdate(req);
                        return;
                    case "send":
                        processMessageSend(req);
                        return;
                    case "delete":
                        processMessageDelete(req, res);
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

        ConversationDAO cDao = (ConversationDAO) getServletContext().getAttribute((C.CONV_DAO));
        Integer invPending = cDao.countInvitations(user.getId());
        req.setAttribute(R.INVITATIONS_PENDING, invPending);

        RequestDispatcher respJSP = req.getRequestDispatcher(MAIN_PAGE);
        UserDAO uDao = (UserDAO) getServletContext().getAttribute(C.USER_DAO);
        Collection<ShortUserInfo> friends = uDao.listFriends(user.getId());
        String fString = friends.stream().map(ShortUserInfo::getUsername).sorted().collect(Collectors.joining(","));
        req.setAttribute(R.FRIEND_STRING, fString);
        respJSP.forward(req, res);
        return;
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private void processUserUpdate(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute(S.USER);
        UserDAO userDAO = (UserDAO) getServletContext().getAttribute(C.USER_DAO);

        String userAction = req.getParameter("action");
        if (userAction != null) {
            switch (userAction) {
                case "addfriend":
                    log.info("Adding friend to user '{}'", user.getUsername());
                    userDAO.addFriend(user.getId(), parseOrNull(req.getParameter("id")));
                    break;
                case "remfriend":
                    log.info("Removing friend from user '{}'", user.getUsername());
                    userDAO.removeFriend(user.getId(), parseOrNull(req.getParameter("id")));
                    break;
                default:
                    return;
            }
            req.getRequestDispatcher(USER_SEARCH_SERVLET).forward(req, res);        // Send back updated friend list for current user
            return;
        } else {
            String newFullName = req.getParameter("fullname");
            String newEmail = req.getParameter("email");
            if (newFullName == null || newFullName.equals("")) newFullName = user.getUsername();
            if (newEmail == null) newEmail = "";
            user.setFullName(newFullName);
            user.setEmail(newEmail);
            user.setRegComplete(true);
            log.info("Updating user info for user '{}'", user.getUsername());
            userDAO.updateUserInfo(user);
            res.sendRedirect(DETAILS_PAGE);
            return;
        }
    }

    private void processMessageUpdate(HttpServletRequest req) {
        Long id = parseOrNull(req.getParameter("id"));
        if (id == null) return;
        Boolean unread = ofNullable(req.getParameter("unread")).map(String::toLowerCase).map("true"::equals).orElse(null);
        String newText = req.getParameter("newText");

        MessageDAO mDao = (MessageDAO) getServletContext().getAttribute(C.MSG_DAO);
        // Null parameters are ignored
        mDao.updateMessage(id, newText, unread);            // TODO: message update authorization, update timestamp
    }

    private void processMessageSend(HttpServletRequest req) {
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
        mDao.sendMessage(newMsg);   // TODO: check conversation status
    }


    private void processMessageDelete(HttpServletRequest req, HttpServletResponse res) {
        Long id = parseOrNull(req.getParameter("msgId"));
        if (id == null) return;

        MessageDAO mDao = (MessageDAO) getServletContext().getAttribute(C.MSG_DAO);
        Message msg = mDao.getMessageById(id);
        if (msg == null) return;

        int deleteMethod = 0;
        String username = ((User) req.getSession().getAttribute(S.USER)).getUsername();
        if (msg.getConversationId() <= 0) {       // Private
            if (msg.getTo().equals(username))
                deleteMethod = 1;               // Delete
        } else {                                // Conversation
            ConversationDAO cDao = (ConversationDAO) getServletContext().getAttribute(C.CONV_DAO);
            Conversation conv = cDao.getConversation(msg.getConversationId());
            if (conv == null)                   // Not existing
                deleteMethod = 1;
            else if (conv.getStarter().equals(username))
                deleteMethod = 2;               // Wipe
        }
        switch (deleteMethod) {
            case 1:
                mDao.deleteMessage(id);
                break;
            case 2:
                mDao.updateMessage(id, "***", null);
                break;
            default:
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}