package model.dao;

/**
 * Global DAO for properties files (testing)
 */
public class PropsDAO implements GlobalDAO {
    private final UserDAO userDAO = new UserDAO_props();
    private final CredentialsDAO credsDAO = new CredsDAO_props();

    @Override
    public UserDAO instantiateUserDAO() {
        return userDAO;
    }

    @Override
    public CredentialsDAO instantiateCredentialsDAO() {
        return credsDAO;
    }
}