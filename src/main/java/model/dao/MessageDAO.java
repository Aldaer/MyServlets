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
        Long getId();

        Long getRefId();

        String getFrom();

        String getTo();

        Timestamp getMinTime();

        Timestamp getMaxTime();

        Long getConvId();

        String getTextLike();

        Integer getOffset();

        Integer getLimit();

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
