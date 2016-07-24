package model.dao;

/**
 * Global DAO for properties files (testing)
 */
public class PropsDAO implements GlobalDAO {
    private final UserDAO userDAO = new UserDAO_props();
    private final CredentialsDAO credsDAO = new CredsDAO_props();

    @Override
    public UserDAO getUserDAO() {
        return userDAO;
    }

    @Override
    public CredentialsDAO getCredentialsDAO() {
        return credsDAO;
    }

    @Override
    public MessageDAO getMessageDAO() {
        return null;
    }
}
