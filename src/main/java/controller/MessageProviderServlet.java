package controller;

import controller.utils.JsonNullableGenerator;
import lombok.extern.slf4j.Slf4j;
import model.dao.Message;
import model.dao.MessageDAO;
import model.dao.MessageDAO.MessageFilter;
import model.dao.User;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.TreeSet;

import static controller.AttributeNames.C;
import static controller.AttributeNames.S;
import static controller.PageURLs.MESSAGE_SERVLET;
import static controller.utils.IntegerUtils.*;

/**
 * Answers JSON requests, such as getting message lists
 */
@Slf4j
@WebServlet(MESSAGE_SERVLET)
public class MessageProviderServlet extends HttpServlet {
    private static final String JSON_TYPE = "application/json";
    private static final String JSON_TIME_FORMAT = "yyyy-MM-dd’T'HH:mm:ss.SSSZ";

    private static final String MSG_TYPE = "type";     // Comma-delimited: "from", "to" // TODO: add "conv" etc.
    private static final String MSG_OFFSET = "offset"; // # of messages to skip
    private static final String MSG_LIMIT = "limit";   // # of messages to send
    private static final int MAX_MESSAGES_RETURNED = 100;

    private static final JsonGeneratorFactory JF = Json.createGeneratorFactory(null);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String msgTypes = req.getParameter(MSG_TYPE);

        String cUserName = ((User) req.getSession().getAttribute(S.USER)).getUsername();

        MessageDAO msgDao = (MessageDAO) req.getServletContext().getAttribute(C.MSG_DAO);
        final MessageFilter.Builder mBuilder = MessageFilter.newBuilder();

        mBuilder.setOffset(withinRangeOrMin(parseOrNull(req.getParameter(MSG_OFFSET)), 0, Integer.MAX_VALUE));
        mBuilder.setLimit(withinRangeOrMax(parseOrNull(req.getParameter(MSG_LIMIT)), 0, MAX_MESSAGES_RETURNED));

        if (msgTypes != null) {
            if (msgTypes.contains("from")) mBuilder.setFrom(cUserName);
            if (msgTypes.contains("to")) mBuilder.setTo(cUserName);
        }

        TreeSet<Message> messagesByTimestamp = new TreeSet<>(Message.byTime);
        messagesByTimestamp.addAll(msgDao.getMessages(mBuilder));
        int totalCount = msgDao.countMessages(mBuilder);            // May be more than messagesByTimestamp.size() due to offset and limit

        res.setContentType(JSON_TYPE);

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
}

