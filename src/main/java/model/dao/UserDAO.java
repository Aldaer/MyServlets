package model.dao;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Common interface to support user DAO functionality
 */
public interface UserDAO {
    /**
     * Returns user id for given username, empty if no such user. User name is NOT case-sensitive
     * @param Username User name
     * @return User id or empty optional
     */
    Optional<Long> getIdByName(String Username);

    /**
     * Returns user object for given id, null if no such user
     * @param id User id to find
     * @return User object loaded from the database
     */
    @Nullable User getUser(long id);

    /**
     * Checks if id-password pair is valid
     * @param id User id
     * @param password User password
     * @return True if correct password is presented for an existing user, false otherwise
     */
    boolean authenticateUser(long id, String password);

    /**
     * Checks if name-password pair is valid
     * @param name User name
     * @param password User password
     * @return User id on successful authentication, empty otherwise
     */
    default Optional<Long> authenticateUser(String name, String password) {
        Optional<Long> id;
        id = getIdByName(name);
        if (id.isPresent() && ! authenticateUser(id.get(), password)) id = Optional.empty();
        return id;
    }


}
