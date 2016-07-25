package model.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.List;

/**
 * Counts and retrieves messages in the message database
 */
public interface MessageDAO {
    /**
     * Returns all messages corresponding to the provided criteria
     * @param constraint Message filtering criteria
     * @return List of messages from database
     */
    List<Message> getMessages(MessageFilter constraint);

    /**
     * Counts all messages corresponding to the provided criteria.
     * {@code Limit} and {@code offset} are IGNORED.
     * @param constraint Message filtering criteria
     * @return Number of messages. -1 on database error.
     */
    int countMessages(MessageFilter constraint);

    /**
     *
     */
    interface MessageFilter {
        /**
         * Unique message id
         */
        Long getId();

        /**
         * For replies: message id of the "parent" message.
         * Otherwise, 0
         */
        Long getRefId();

        /**
         * Message author's username
         */
        String getFrom();

        /**
         * Message recipients's username. Null for messages in conversations
         */
        String getTo();

        /**
         * Minimum UTC timestamp, inclusive
         */
        Timestamp getMinTime();

        /**
         * Maximum UTC timestamp, inclusive
         */
        Timestamp getMaxTime();

        /**
         * Conversation id. 0 for unread private messages, -1 for read private messages
         */
        Long getConvId();

        /**
         *  SQL text pattern, e.g. '%unday%'
         */
        String getTextLike();

        /**
         * Skip first N messages
         */
        Integer getOffset();

        /**
         * Return no more than M messages
         */
        Integer getLimit();

        /**
         * Sort messages by a field indicated by name.
         * Note this is NOT a column name, but annotated {@link model.dao.databases.StoredField} name.
         */
        String getSortField();

        static Builder newBuilder() {
            return new Builder();
        }

        /**
         * Reusable builder, can be used to build constraints one by one.
         * Can be assigned to {@link MessageFilter} directly or by creating
         * an immutable copy. Default value of all constraints is {@code null}
         * meaning no constraints.
         */
        @Getter
        @Setter
        @Accessors(chain = true)
        class Builder implements MessageFilter, Cloneable {
            private Long id;
            private Long refId;
            private String from;
            private String to;
            private Timestamp minTime;
            private Timestamp maxTime;
            private Long convId;
            private String textLike;
            private Integer offset;
            private Integer limit;
            private String sortField;

            /**
             * Creates a copy of current state oif this builder.
             * @return Cloned {@link MessageFilter} object
             */
            public MessageFilter createCopy() {
                try {
                    return (MessageFilter) this.clone();
                } catch (CloneNotSupportedException e) {
                    return null; // Should never happen
                }
            }

            private Builder() {
            }
        }
    }


}
