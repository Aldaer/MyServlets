package model.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.List;

/**
 * Gets
 */
public interface MessageDAO {
    List<Message> getMessages(MessageFilter constraint);

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

        Integer getSkip();

        Integer getMaxReturned();

        static Builder newBuilder() {
            return new Builder();
        }

        /**
         * Reusable builder, can be used to build constraints one by one.
         * Can be assigned to {@link MessageFilter} directly or by creating
         * a permanent copy. Default value of all constraints is null
         * meaning no constraints.
         */
        @Getter
        @Setter
        @Accessors(chain = true)
        public class Builder implements MessageFilter, Cloneable {
            private Long id;
            private Long refId;
            private String from;
            private String to;
            private Timestamp minTime;
            private Timestamp maxTime;
            private Long convId;
            private String textLike;
            private Integer skip;
            private Integer maxReturned;

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
