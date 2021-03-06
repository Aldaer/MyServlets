package model.dao;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

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
     * Returns a set of user's friends from database
     * @param id User id
     * @return id's of user's friends, or empty array if none
     */
    @NotNull long[] getFriendIds(long id);

    /**
     * Updates info for user with name == {@code user.username } in the database.
     * @param user User object with updated fields. Users with unknown {@code username} are ignored.
     */
    void updateUserInfo(@NotNull User user);

    /**
     * Outputs a collection of user data finding users by 'LIKE %partialName%" clause
     * @param partialName Part of the user's full name
     * @param limit Maximum number of users to output
     * @return Collection of user info
     */
    Collection<ShortUserInfo> listUsers(@Nullable String partialName, int limit);

    /**
     * Outputs a collection of user data finding users through {@code friends} table
     * @param currentUserId Id of user to find friends
     * @return Collection of user info
     */
    Collection<ShortUserInfo> listFriends(long currentUserId);

    /**
     * Outputs a collection of user data finding users through {@code conversation_participants} table
     * @param convId Id of the conversation to find participants
     * @return Collection of user info
     */
    Collection<ShortUserInfo> listParticipants(long convId);

    /**
     * Adds new friend to user's friend list
     * @param id User to add friend to
     * @param friendId User being added to friend list
     */
    void addFriend(long id, Long friendId);

    /**
     * Removes user from another user's friend list
     * @param id User to remove friend from
     * @param friendId User being removed from friend list
     */
    void removeFriend(long id, Long friendId);
}
