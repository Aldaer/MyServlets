package model.dao;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

/**
 * Gets
 */
public interface MessageDAO {
    List<Message> getMessages(MessageConstraint constraint);

    /**
     *
     */
    interface MessageConstraint {
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

        static Builder builder() {
            return new Builder();
        }

        @Getter
        @Setter
        class Builder implements MessageConstraint {
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

            public MessageConstraint create() {
                return this;
            }

            private Builder() {
            }
        }
    }


}
