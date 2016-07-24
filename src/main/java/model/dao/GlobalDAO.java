package model.dao;

/**
 * Universal DAO provider. May or may not be using a database. Allows grouped access to various DAOs using the same database.
 */
public interface GlobalDAO {
    UserDAO getUserDAO();
    CredentialsDAO getCredentialsDAO();
    MessageDAO getMessageDAO();
}
