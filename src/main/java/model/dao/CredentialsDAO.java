package model.dao;

/**
 * Gets credential info for login. Allows authenticating users by name and password
 */
public interface CredentialsDAO {
    /**
     * Gets uiser credential from underlying data source
     * @param username User login, NOT case-sensitive
     * @return {@link Credentials} object
     */
    Credentials getCredentials(String username);

    /**
     * States if user passwords are stored as a randomly salted hash values
     * @param doUse True if hashed, false if plain-text
     */
    void useSaltedHash(boolean doUse);
}
