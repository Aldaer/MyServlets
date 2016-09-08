package model.dao;

import org.springframework.context.annotation.Bean;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Universal DAO provider. May or may not be using a database. Allows grouped access to various DAOs using the same database.
 */
public interface GlobalDAO {
    @Bean
    UserDAO getUserDAO();

    @Bean
    CredentialsDAO getCredentialsDAO();

    @Bean
    MessageDAO getMessageDAO();

    @Bean
    ConversationDAO getConversationDAO();

    default Timestamp currentUtcTimestamp() {
        return Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC));
    }
}
