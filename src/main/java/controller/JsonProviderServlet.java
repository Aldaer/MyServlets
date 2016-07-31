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
import static controller.PageURLs.MESSAGE_PROVIDER_SERVLET;
import static controller.PageURLs.USER_SEARCH_SERVLET;
import static controller.utils.MyStringUtils.*;

/**
 * Answers JSON requests, such as getting message or user lists
 */
@Slf4j
@WebServlet({MESSAGE_PROVIDER_SERVLET, USER_SEARCH_SERVLET})
public class JsonProviderServlet extends HttpServlet {
    private static final String MSG_QUERY_TYPE = "type";     // Comma-delimited: "from", "to" // TODO: add more filters as required
    private static final String MSG_QUERY_CONV = "convId";   // Comma-delimited conversation id's

    private static final String USR_QUERY = "query";         // Partial name of a user to find
    private static final String USR_DETAILS = "details";     // Exact username of a user to get details

    private static final String QUERY_OFFSET = "offset"; // # of messages to skip
    private static final String QUERY_LIMIT = "limit";   // # of messages to send
    private static final int MAX_OBJECTS_RETURNED = 100;
    private static final int MIN_QUERY_LENGTH = 2;       // # of characters in %LIKE% query

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
            case USER_SEARCH_SERVLET:
                processUserQuery(req, res);
                break;
        }
    }

    private void processMessageRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String cUserName = ((User) req.getSession().getAttribute(S.USER)).getUsername();

        MessageDAO msgDao = (MessageDAO) getServletContext().getAttribute(C.MSG_DAO);
        final MessageFilter.Builder mBuilder = MessageFilter.newBuilder();

        mBuilder.setOffset(withinRangeOrMin(parseOrNull(req.getParameter(QUERY_OFFSET)), 0, Integer.MAX_VALUE));
        mBuilder.setLimit((int)withinRangeOrMax(parseOrNull(req.getParameter(QUERY_LIMIT)), 0, MAX_OBJECTS_RETURNED));

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
        messagesByTimestamp.addAll(msgDao.getMessages(mBuilder));
        int totalCount = msgDao.countMessages(mBuilder);            // May be more than messagesByTimestamp.size() due to offset and limit

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

        String reqName = req.getParameter(USR_DETAILS);
        if (reqName != null) {                                  // Specific user requested
            User reqUser = uDao.getUser(reqName);
            gen.writeStartObject();
            if (reqUser != null) gen
                    .write("exists", true)
                    .write("id", reqUser.getId())
                    .write("fullName", reqUser.getFullName())
                    .write("email", reqUser.getEmail());
            else gen.write("exists", false);
            gen.writeEnd().close();
        } else {                                                // User search requested
            String userLike = req.getParameter(USR_QUERY);
            if (userLike == null || userLike.length() < MIN_QUERY_LENGTH) return;
            int limit = (int)withinRangeOrMax(parseOrNull(req.getParameter(QUERY_LIMIT)), 0, MAX_OBJECTS_RETURNED);

            Collection<ShortUserInfo> userList = uDao.listUsers(userLike, limit);

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
    }
}

