package model.dao;

/**
 * Gets credential info for login. Allows authenticating users by name and password
 */
public interface CredentialsDAO {
    /**
     * Gets uiser credential from underlying data source
     * @param username User login name, NOT case-sensitive
     * @return {@link Credentials} object
     */
    Credentials getCredentials(String username);

    /**
     * States if user passwords are stored as a randomly salted hash values.
     * @param doUse True if hashed, false if plain-text
     */
    void useSaltedHash(boolean doUse);

    /**
     * Checks if user exists in main AND temporary credentials tables
     * @param username User login name, NOT case-sensitive
     * @return True if uses exists in any of the tables
     */
    boolean checkIfUserExists(String username);

    /**
     * Creates a user in a TEMPORARY table setting UTC timestamp for time of creation
     * @param username User login name, converted to lowercase
     * @return True if successful, false if such temporary user already exists
     */
    boolean createTemporaryUser(String username);

    /**
     * Purges credentials from TEMPORARY user table created before indicated time (UTC)
     * @param timeThreshold UTC threshold time in millis. All records older than this will be purged
     */
    void purgeTemporaryUsers(long timeThreshold);
}
