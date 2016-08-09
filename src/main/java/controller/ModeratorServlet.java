package controller;

import lombok.extern.slf4j.Slf4j;
import model.dao.ConversationDAO;
import model.dao.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static controller.PageURLs.MODERATOR_SERVLET;
import static controller.utils.MyStringUtils.parseOrDefault;

/**
 * Moderator servlet, processes delivery of user invitations and bans
 */
@Slf4j
@WebServlet(MODERATOR_SERVLET)
public class ModeratorServlet extends HttpServlet {
    private static final int BAN_USER = 1;
    private static final int INVITE_USER = 2;


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setStatus(processModeratorAction(req));
    }

    private int processModeratorAction(HttpServletRequest req) {
        User user = (User) req.getSession().getAttribute(AttributeNames.S.USER);
        long convId = parseOrDefault(req.getParameter("convId"), -1L);
        int action = parseOrDefault(req.getParameter("action"), -1);
        if (convId < 0 || action < 0) return HttpServletResponse.SC_BAD_REQUEST;

        ConversationDAO cDao = (ConversationDAO) getServletContext().getAttribute(AttributeNames.C.CONV_DAO);
        if (!cDao.listOwnConversations(user.getUsername()).stream().filter(conv -> conv.getId() == convId).findAny().isPresent())
            return HttpServletResponse.SC_FORBIDDEN;

        switch (action) {
            case BAN_USER:
                long uid = parseOrDefault(req.getParameter("uid"), -1L);
                if (uid <= 0) return HttpServletResponse.SC_BAD_REQUEST;
                cDao.leaveConversation(convId, uid);
                break;

            default:
                return HttpServletResponse.SC_BAD_REQUEST;
        }
        return HttpServletResponse.SC_OK;
    }
}
