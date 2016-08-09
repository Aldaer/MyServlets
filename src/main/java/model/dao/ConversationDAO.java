package model.dao;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Creates and retrieves conversations
 */
public interface ConversationDAO {
    /**
     * Returns conversation object for given id, null if no such conversation
     * @param id Conversation to find
     * @return Conversation object loaded from database
     */
    @Nullable Conversation getConversation(long id);

    /**
     * Returns conversations in which the indicated user participates
     * @param userId Id of the participating user
     * @return Collection of conversation objects loaded from database
     */
    @NotNull Collection<Conversation> listConversations(long userId);

    /**
     * Returns conversations into which the indicated user was invited
     * @param userId Id of the participating user
     * @return Collection of conversation objects loaded from database
     */
    @NotNull Collection<Conversation> listInvites(long userId);


    /**
     * Returns conversations which the indicated user started
     * @param username Login name of the starting user
     * @return Collection of conversation objects loaded from database
     */
    @NotNull Collection<Conversation> listOwnConversations(String username);

    /**
     * Creates new conversation and stores in the database.
     * Note that conversation is bound to user name, not user id, to
     * preserve conversations started by deleted users.
     * @param name Conversation name
     * @param desc Conversation description
     * @param starter User who started the conversation
     * @return Conversation object
     */
    @Nullable Conversation createConversation(String name, String desc, User starter);

    /**
     * Indicated user joins the conversation. Joining removes user from invited to the same conversation
     * @param convId Conversation id
     * @param userId User id
     */
    void joinConversation(long convId, long userId);

    /**
     * Indicated user gets invited to conversation
     * @param convId Conversation id
     * @param userId User id
     */
    void inviteToConversation(long convId, long userId);

    /**
     * Indicated user leaves the conversation
     * @param convId Conversation id
     * @param userId User id
     */
    void leaveConversation(long convId, long userId);

    /**
     * Indicated user is no longer invited to conversation
     * @param convId Conversation id
     * @param userId User id
     */
    void removeInvite(long convId, long userId);

    /**
     * Deletes the conversation
     * @param convId Conversation to delete
     */
    void deleteConversation(long convId);

    /**
     * Accepts or declines invitations to one or more conversations for a specified user
     * @param userId User id
     * @param accept true = accept, false = decline
     * @param inviteList conversation ids, comma-separated
     * @return List of remaining invitations after accept/decline
     */
    Collection<Conversation> acceptOrDeclineInvites(long userId, boolean accept, long[] inviteList);
}
