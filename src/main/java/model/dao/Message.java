package model.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.dao.common.Stored;
import model.dao.common.StoredField;

import java.sql.Timestamp;

/**
 * Message from one user to another or into community
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Stored {
    @StoredField(auto = true, column = "id")
    long id = 0;

    /**
     * refId == 0 when this message is not a reply to anything
     */
    @Setter
    @StoredField("refid")
    long refId;

    /**
     * Message author, never null
     */
    @Setter
    @StoredField("u_from")
    String from;

    /**
     * Message recipient, null when posting into a conversation
     */
    @Setter
    @StoredField("u_to")
    String to;

    /**
     * Auto-generated by database
     */
    @StoredField(auto = true, column = "m_time")
    Timestamp utcTimestamp;

    /**
     * Conversation id. 0 = private message, unread. -1 = private message, read.
     */
    @Setter
    @StoredField("conversation_id")
    long conversationId;

    @Setter
    @StoredField("text")
    String text;
}