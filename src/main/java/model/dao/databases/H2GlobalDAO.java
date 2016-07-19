package model.dao.databases;

import lombok.extern.slf4j.Slf4j;
import model.dao.*;
import model.dao.common.ResultSetParser;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * Global DAO for H2 database.
 * Actual User and Credential dao returned by this Global DAO are singletons. By default they use connection source
 * configured in GlobalDAO at the time they are requested.
 */
@Slf4j
public class H2GlobalDAO implements GlobalDAO, DatabaseDAO {
    private static final String GET_CREDS_BY_LOGIN_NAME = "SELECT dpassword FROM credentials WHERE (username=?)";
    private static final String GET_USER_BY_LOGIN_NAME = "SELECT * FROM users WHERE (username=?)";
    private static final String GET_USER_BY_ID = "SELECT * FROM users WHERE (id=?)";

    private Supplier<Connection> cSource;
    private final H2user userDAO;
    private final H2credentials credsDAO;

    private class H2user implements UserDAO, DatabaseDAO {
        private volatile Supplier<Connection> usersSource;

        @Override
        public @Nullable User getUser(long id) {
            return getUserByAnyKey(GET_USER_BY_ID, id);
        }

        @Override
        public @Nullable User getUser(String username) {
            return getUserByAnyKey(GET_USER_BY_LOGIN_NAME, username);
        }

        private User getUserByAnyKey(String sql, Object key) {
            try (Connection conn = usersSource.get(); PreparedStatement st = conn.prepareStatement(sql)) {
                st.setObject(1, key);
                log.trace("Executing query: {} <== ({})", sql, key);
                ResultSet rs = st.executeQuery();
                rs.next();
                return ResultSetParser.reconstructObject(rs, User::new);
            } catch (SQLException e) {
                log.error("Error getting data for user [{}]: {}", key, e);
                return null;
            }
        }

        @Override
        public void useConnectionSource(Supplier<Connection> src) {
            usersSource = src;
        }
    }

    private class H2credentials implements CredentialsDAO, DatabaseDAO {
        private volatile Supplier<Connection> credsSource;
        private volatile boolean saltedHash = false;

        @Override
        public Credentials getCredentials(String username) {
            String lcName = username.toLowerCase();
            try (Connection conn = credsSource.get(); PreparedStatement st = conn.prepareStatement(GET_CREDS_BY_LOGIN_NAME)) {
                st.setObject(1, lcName);
                log.trace("Executing query: {} <== ({})", GET_CREDS_BY_LOGIN_NAME, lcName);
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
    }

    public H2GlobalDAO() {
        userDAO = new H2user();
        credsDAO = new H2credentials();
    }

    @Override
    public H2user instantiateUserDAO() {
        userDAO.useConnectionSource(cSource);
        return userDAO;
    }

    @Override
    public H2credentials instantiateCredentialsDAO() {
        credsDAO.useConnectionSource(cSource);
        return credsDAO;
    }

    @Override
    public void useConnectionSource(Supplier<Connection> src) {
        cSource = src;
    }
}
