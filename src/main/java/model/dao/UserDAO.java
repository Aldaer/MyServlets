package model.dao;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

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
     * Returns user object for given username, null if no such user. User name is NOT case-sensitive
     * @param username User name
     * @return User object loaded from the database
     */
    @Nullable User getUser(@Nullable String username);

    /**
     * Updates info for user with name == {@code user.username } in the database.
     * @param user User object with updated fields. Users with unknown {@code username} are ignored.
     */
    void updateUserInfo(@NotNull User user);

    /**
     * Outputs a collection of user data finding users by 'LIKE %partialName%" clause
     * @param partialName Part of the user's full name
     * @param limit Maximum number of users o output
     * @return Map of "username"-"full name" pairs
     */
    Map<String, String> listUsers(@Nullable String partialName, int limit);
}
