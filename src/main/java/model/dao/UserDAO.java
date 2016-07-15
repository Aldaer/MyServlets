package model.dao;

import model.utils.CryptoUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Common interface to support user DAO functionality
 */
public interface UserDAO {

    /**
     * Returns user object for given id, null if no such user
     * @param id User id to find
     * @return User object loaded from the database
     */
    Optional<User> getUser(long id);

    /**
     * Returns user id for given username, empty if no such user. User name is NOT case-sensitive
     * @param username User name
     * @return User id or empty optional
     */
    Optional<User> getUser(String username);

    /**
     * Checks if username-password pair is valid
     * @param user User object
     * @param password User password (clear-text)
     * @return True if correct password is presented for an existing non-empty user, false otherwise
     */
    ;

    boolean authenticateUser(Optional<User> user, String password);
}
