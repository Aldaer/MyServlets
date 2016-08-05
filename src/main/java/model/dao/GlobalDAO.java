package model.dao;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Universal DAO provider. May or may not be using a database. Allows grouped access to various DAOs using the same database.
 */
public interface GlobalDAO {
    UserDAO getUserDAO();

    CredentialsDAO getCredentialsDAO();

    MessageDAO getMessageDAO();

    ConversationDAO getConversationDAO();

    default Timestamp currentUtcTimestamp() {
        return Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC));
    }
}
