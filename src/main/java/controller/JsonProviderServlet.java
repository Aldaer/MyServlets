package controller;

import controller.utils.JsonNullableGenerator;
import controller.utils.MyStringUtils;
import lombok.extern.slf4j.Slf4j;
import model.dao.*;
import model.dao.MessageDAO.MessageFilter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Stream;

import static controller.AttributeNames.C;
import static controller.AttributeNames.S;
import static controller.MiscConstants.JSON_TYPE;
import static controller.PageURLs.*;
import static controller.utils.MyStringUtils.*;

/**
 * Answers JSON requests, such as getting message or user lists
 */
@Slf4j
@WebServlet({MESSAGE_PROVIDER_SERVLET, CONVERSATION_PROVIDER_SERVLET, USER_SEARCH_SERVLET})
public class JsonProviderServlet extends HttpServlet {
    private static final String MSG_QUERY_TYPE = "type";     // Comma-delimited: "from", "to" // TODO: add more filters as required
    private static final String MSG_QUERY_CONV = "convId";   // Comma-delimited conversation id's

    private static final String USR_QUERY = "query";         // Partial name of a user to find
    private static final String USR_DETAILS = "details";     // Exact username of a user to get DETAILS
    private static final String USR_FRIENDS = "friends";     // Send a list of current user's friends

    private static final String QUERY_OFFSET = "offset"; // # of messages to skip
    private static final String QUERY_LIMIT = "limit";   // # of messages to send
    private static final int MAX_OBJECTS_RETURNED = 100;
    private static final int MIN_QUERY_LENGTH = 2;       // # of characters in %LIKE% query

    private static final String CONV_MODE = "mode";

    private static final JsonGeneratorFactory JF = Json.createGeneratorFactory(null);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        log.debug("Processing JSON request: {}", req.getQueryString());
        res.setContentType(JSON_TYPE);

        switch (req.getRequestURI()) {
            case MESSAGE_PROVIDER_SERVLET:
                processMessageRequest(req, res);
                break;
            case CONVERSATION_PROVIDER_SERVLET:
                processConversationRequest(req, res);
                break;
            case USER_SEARCH_SERVLET:
                processUserQuery(req, res);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }

    private void processMessageRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String cUserName = ((User) req.getSession().getAttribute(S.USER)).getUsername();

        MessageDAO msgDao = (MessageDAO) getServletContext().getAttribute(C.MSG_DAO);
        final MessageFilter.Builder mBuilder = MessageFilter.newBuilder();

        mBuilder.setOffset(withinRangeOrMin(parseOrNull(req.getParameter(QUERY_OFFSET)), 0, Integer.MAX_VALUE));
        mBuilder.setLimit((int) withinRangeOrMax(parseOrNull(req.getParameter(QUERY_LIMIT)), 0, MAX_OBJECTS_RETURNED));

        String msgTypes = req.getParameter(MSG_QUERY_TYPE);
        if (msgTypes != null) {
            if (msgTypes.contains("from")) mBuilder.setFrom(cUserName);
            if (msgTypes.contains("to")) mBuilder.setTo(cUserName);
        }

        String convIds = req.getParameter(MSG_QUERY_CONV);
        if (convIds != null) {
            mBuilder.setConvId(Stream.of(convIds.split(",")).map(MyStringUtils::parseOrNull).filter(Objects::nonNull).toArray(Long[]::new));
        }

        TreeSet<Message> messagesByTimestamp = new TreeSet<>(Message.byTime);
        int totalCount = msgDao.countMessages(mBuilder);            // May be more than messagesByTimestamp.size() due to offset and limit
        messagesByTimestamp.addAll(msgDao.getMessages(mBuilder));

        final JsonGenerator gen = new JsonNullableGenerator(JF, res.getOutputStream());

        log.debug("Outputting {} messages out of {}", messagesByTimestamp.size(), totalCount);
        gen.writeStartObject();
        gen.write("totalCount", totalCount);
        gen.writeStartArray("messages");
        messagesByTimestamp.stream().forEachOrdered(msg -> gen
                .writeStartObject()
                .write("id", msg.getId())
                .write("refId", msg.getRefId())
                .write("from", msg.getFrom())
                .write("to", msg.getTo())
                .write("utcTimestamp", msg.getUtcTimestamp().getTime())
                .write("conversationId", msg.getConversationId())
                .write("text", msg.getText())
                .writeEnd()
        );
        gen.writeEnd().writeEnd().close();
    }

    private void processUserQuery(HttpServletRequest req, HttpServletResponse res) throws IOException {
        final UserDAO uDao = (UserDAO) getServletContext().getAttribute(C.USER_DAO);
        final JsonGenerator gen = new JsonNullableGenerator(JF, res.getOutputStream());

        boolean allFriendsRq;
        String friendsRequest = req.getParameter(USR_FRIENDS);
        long currentUserId = ((User) req.getSession(false).getAttribute(S.USER)).getId();

        if ("ids".equals(friendsRequest)) {   // Friend list requested, ids only
            long[] friends = uDao.getFriendIds(currentUserId);
            log.trace("Found friends: {}", friends.length);
            gen.writeStartArray();
            for (long frId : friends)
                gen.write(frId);
            gen.writeEnd().close();
            return;
        } else
            allFriendsRq = "all".equals(friendsRequest);

        String reqName = req.getParameter(USR_DETAILS);
        if (reqName != null) {                                  // Specific user requested
            User reqUser = uDao.getUser(reqName);
            gen.writeStartObject();
            if (reqUser != null) gen
                    .write("exists", true)
                    .write("id", reqUser.getId())
                    .write("username", reqName)
                    .write("fullName", reqUser.getFullName())
                    .write("email", reqUser.getEmail());
            else gen
                    .write("exists", false)
                    .write("username", "???");
            gen.writeEnd().close();
            return;
        }

        Collection<ShortUserInfo> userList;                     // User list requested

        Long convId = parseOrNull(req.getParameter(MSG_QUERY_CONV));
        if (convId != null) {                                   // Conversation participants request
            userList = uDao.listParticipants(convId);
        } else {

            // User search request or unknown request
            String userLike = req.getParameter(USR_QUERY);
            if ((userLike == null || userLike.length() < MIN_QUERY_LENGTH) && !allFriendsRq) return;
            int limit = (int) withinRangeOrMax(parseOrNull(req.getParameter(QUERY_LIMIT)), 0, MAX_OBJECTS_RETURNED);

            userList = allFriendsRq ? uDao.listFriends(currentUserId) : uDao.listUsers(userLike, limit);
        }

        log.debug("Outputting {} found users", userList.size());
        gen.writeStartObject();
        gen.writeStartArray("users");
        userList.stream().sorted((u1, u2) -> u1.getUsername().compareTo(u2.getUsername()))
                .forEachOrdered(usr -> gen
                        .writeStartObject()
                        .write("id", usr.getId())
                        .write("username", usr.getUsername())
                        .write("fullName", usr.getFullName())
                        .writeEnd()
                );
        gen.writeEnd().writeEnd().close();
    }

    private void processConversationRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int convMode = parseOrDefault(req.getParameter(CONV_MODE), -1);

        final ConversationDAO convDao = (ConversationDAO) getServletContext().getAttribute(C.CONV_DAO);
        User currentUser = (User) req.getSession().getAttribute(S.USER);

        Collection<Conversation> convList;

        switch (convMode) {
            case 0:
                convList = convDao.listOwnConversations(currentUser.getUsername());
                break;
            case 1:
                convList = convDao.listConversations(currentUser.getId());
                break;
            case 2:
                convList = convDao.listInvites(currentUser.getId());
                break;
            case 10:
                String convName = req.getParameter("name");
                String convDesc = req.getParameter("desc");
                convDao.createConversation(convName, convDesc, currentUser);
                convList = convDao.listOwnConversations(currentUser.getUsername());
                break;
            default:
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
        }

        log.debug("Outputting {} found conversations", convList.size());

        final JsonGenerator gen = new JsonNullableGenerator(JF, res.getOutputStream());

        gen.writeStartArray();
        convList.stream().forEach(conv -> gen
                .writeStartObject()
                .write("id", conv.getId())
                .write("name", conv.getName())
                .write("desc", conv.getDesc())
                .write("starter", conv.getStarter())
                .write("started", conv.getStarted().getTime())
                .writeEnd());
        gen.writeEnd().close();
    }


}

