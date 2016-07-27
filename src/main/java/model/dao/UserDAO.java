package model.dao;

import org.jetbrains.annotations.NotNull;
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
}
