package controller;

import lombok.extern.slf4j.Slf4j;
import model.dao.Message;
import model.dao.MessageDAO;
import model.dao.MessageDAO.MessageFilter;
import model.dao.User;
import org.jetbrains.annotations.Nullable;

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

    private static final JsonGeneratorFactory jsonFactory = Json.createGeneratorFactory(null);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String msgTypes = req.getParameter(MSG_TYPE);

        String cUserName = ((User) req.getSession().getAttribute(S.USER)).getUsername();

        MessageDAO msgDao = (MessageDAO) req.getServletContext().getAttribute(C.MSG_DAO);
        final MessageFilter.Builder mBuilder = MessageFilter.newBuilder();

        mBuilder.setOffset(parseOrNull(req.getParameter(MSG_OFFSET)));
        mBuilder.setLimit(parseOrNull(req.getParameter(MSG_LIMIT)));

        if (msgTypes != null) {
            if (msgTypes.contains("from")) mBuilder.setFrom(cUserName);
            if (msgTypes.contains("to")) mBuilder.setFrom(cUserName);
        }

        TreeSet<Message> messagesByTimestamp = new TreeSet<>(Message.byTime);
        messagesByTimestamp.addAll(msgDao.getMessages(mBuilder));
        int totalCount = msgDao.countMessages(mBuilder);            // May be more than messagesByTimestamp.size() due to offset and limit

        res.setContentType(JSON_TYPE);

        JsonGenerator gen = jsonFactory.createGenerator(res.getWriter());

        gen.writeStartObject();
        gen.write("totalCount", totalCount);
        gen.writeStartArray();
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

    private static @Nullable Integer parseOrNull(String s) {
        if (s == null || s.equals("")) return null;
        char[] chars = s.toCharArray();
        int result = 0;
        for (char c: chars) {
            int v = (int) c - 48;
            if (v >= 0 && v <= 9) result = result * 10 + v;
            else return null;
        }
        return result;
    }
}

