package model.dao.databases;

import lombok.extern.slf4j.Slf4j;
import model.dao.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 *
 */
@Slf4j
public class H2GlobalDAO implements GlobalDAO, DatabaseDAO {
    private static final String GET_CREDS_BY_NAME = "SELECT dpassword FROM credentials WHERE (username=?)";

    private Supplier<Connection> cSource;
    private final UserDAO userDAO;
    private final CredentialsDAO credsDAO;

    public H2GlobalDAO() {

        userDAO = null;

        credsDAO = new H2CredentialsDAO() {
            private volatile Supplier<Connection> credsSource = cSource;
            private volatile boolean saltedHash = false;

            @Override
            public Credentials getCredentials(String username) {
                String lcName = username.toLowerCase();
                try (Connection conn = credsSource.get()) {

                    PreparedStatement st = conn.prepareStatement(GET_CREDS_BY_NAME);
                    st.setObject(1, lcName);
                    log.trace("Executing query: {} <== ({})", GET_CREDS_BY_NAME, lcName);
                    ResultSet rs = st.executeQuery();
                    rs.next();
                    Credentials creds = new Credentials(lcName, "", saltedHash);
                    creds.updateFromResultSet(rs);
                    return creds;
                } catch (SQLException e) {
                    log.error("Error getting user credentials: {}", e);
                    return null;
                }
            }
            @Override
            public void useSaltedHash(boolean doUse) {
                saltedHash = doUse;
            }

            @Override
            public void useConnectionSource(Supplier<Connection> src) {
                credsSource = src;
            }
        };

    }

    @Override
    public UserDAO instantiateUserDAO() {
        return userDAO;
    }

    @Override
    public CredentialsDAO instantiateCredentialsDAO() {
        return credsDAO;
    }

    @Override
    public void useConnectionSource(Supplier<Connection> src) {
        cSource = src;
    }
}

/**
 * Actual DAO objects returned by {@link H2GlobalDAO} implement {@link DatabaseDAO},
 * so they may be set to use alternate connection sources
 */
interface H2CredentialsDAO extends CredentialsDAO, DatabaseDAO {}