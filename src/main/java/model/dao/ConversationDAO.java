package model.dao;

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
     * Returns conversations in which a user participates
     * @param userId Id of the participating user
     * @return Conversation object loaded from database
     */
    @Nullable Collection<Conversation> listConversations(long userId);

    /**
     * Returns conversations which a user started
     * @param userId Id of the starting user
     * @return Conversation object loaded from database
     */
    @Nullable Collection<Conversation> listOwnConversations(long userId);

    /**
     * Creates new conversation and stores in the database.
     * Note that conversation is bound to user name, not user id, to
     * preserve conversations started by deleted users.
     * @param name Conversation name
     * @param desc Conversation description
     * @param starter User who started the conversation
     * @return Conversation object
     */
    Conversation createConversation(String name, String desc, User starter);

    /**
     * Indicated user joins the conversation
     * @param convId Conversation id
     * @param userId User id
     */
    void joinConversation(long convId, long userId);

    /**
     * Indicated user leaves the conversation
     * @param convId Conversation id
     * @param userId User id
     */
    void leaveConversation(long convId, long userId);

}
