package model.dao.databases;

import lombok.extern.slf4j.Slf4j;
import model.dao.*;
import model.dao.common.ResultSetParser;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.function.Supplier;

/**
 * Global DAO for H2 database.
 * Actual User and Credential dao returned by this Global DAO are singletons. By default they use connection source
 * configured in GlobalDAO at the time they are requested.
 * Automatic commit on {@code Connection.close()} expected.
 */
@Slf4j
public class H2GlobalDAO implements GlobalDAO, DatabaseDAO {
    private static final String GET_CREDS_BY_LOGIN_NAME = "SELECT dpassword FROM credentials WHERE (username=?)";
    private static final String GET_USER_BY_LOGIN_NAME = "SELECT * FROM users WHERE (username=?)";
    private static final String GET_USER_BY_ID = "SELECT * FROM users WHERE (id=?)";

    private static final String CHECK_IF_USER_EXISTS = "SELECT TOP 1 1 FROM credentials AS C WHERE (C.username=?) UNION SELECT TOP 1 1 FROM temp_credentials AS tc WHERE (TC.username=?);";
    private static final String CREATE_TEMPORARY_ACCOUNT = "INSERT INTO temp_credentials (username, created) VALUES (?, ?)";
    private static final String PURGE_TEMPORARY_ACCOUNTS = "DELETE FROM temp_credentials WHERE (created < ?)";


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
                if (!rs.next()) {
                    log.trace("User '{}' not found", key);
                    return null;
                }
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
                if (!rs.next()) {
                    log.trace("User '{}' not found", username);
                    return null;
                }
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

        @Override
        public boolean checkIfUserExists(String username) {
            try (Connection conn = credsSource.get(); PreparedStatement st = conn.prepareStatement(CHECK_IF_USER_EXISTS)) {
                st.setString(1, username);
                st.setString(2, username);
                log.trace("Executing query: {} <== ({}, {})", CHECK_IF_USER_EXISTS, username, username);
                return st.executeQuery().next();
            } catch (SQLException e) {
                log.error("Error checking if user {} exists: {}", username, e);
                return false;
            }
        }

        @Override
        public boolean createTemporaryUser(String username) {
            String lcName = username.toLowerCase();
            try (Connection conn = credsSource.get(); PreparedStatement st = conn.prepareStatement(CREATE_TEMPORARY_ACCOUNT)) {
                st.setString(1, lcName);
                st.setTime(2, new Time(System.currentTimeMillis()));
                log.trace("Executing query: {} <== ({})", CREATE_TEMPORARY_ACCOUNT, username);
                if (st.executeUpdate() != 1) throw new SQLException("Wrong affected row count");
            } catch (SQLException e) {
                log.error("Error creating temporary account for user: {}", username);
                return false;
            }
            return true;
        }

        @Override
        public void purgeTemporaryUsers(long timeThreshold) {
            try (Connection conn = credsSource.get(); PreparedStatement st = conn.prepareStatement(PURGE_TEMPORARY_ACCOUNTS)) {
                st.setDate(1, new Date(timeThreshold));
                log.trace("Executing query: {} <== ({})", PURGE_TEMPORARY_ACCOUNTS, timeThreshold);
                int numPurged = st.executeUpdate();
                log.debug("Purged {} temporary user accounts", numPurged);
            } catch (SQLException e) {
                log.error("Error purging temporary user accounts: {}", e);
            }
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
