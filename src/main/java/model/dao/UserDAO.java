package model.dao;

import org.jetbrains.annotations.Nullable;

/**
 * Common interface to support user DAO functionality
 */
public interface UserDAO {

    /**
     * Returns user object for given id, null if no such user
     * @param id User id to find
     * @return User object loaded from the database
     */
    @Nullable User getUser(long id);

    /**
     * Returns user id for given username, null if no such user. User name is NOT case-sensitive
     * @param username User name
     * @return User id or empty optional
     */
    @Nullable User getUser(String username);
}
