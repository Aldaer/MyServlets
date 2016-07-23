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
 * Automatic commit on {@code Connection.close()} expected.
 */
@Slf4j
public class H2GlobalDAO implements GlobalDAO, DatabaseDAO {
    private static final String TABLE_USERS = "users";
    private static final String TABLE_CREDENTIALS = "credentials";
    private static final String TABLE_TEMP_CREDENTIALS = "temp_credentials";
    private static final String TABLE_ROLES = "user_roles";
    private static final String DEFAULT_ROLE = "authenticated-user";

    private static final String GET_CREDS_BY_LOGIN_NAME = "SELECT dpassword FROM "+ TABLE_CREDENTIALS + " WHERE (username=?)";
    private static final String GET_USER_BY_LOGIN_NAME = "SELECT * FROM " + TABLE_USERS + " WHERE (username=?)";
    private static final String GET_USER_BY_ID = "SELECT * FROM " + TABLE_USERS + " WHERE (id=?)";

    private static final String CHECK_IF_USER_EXISTS = "SELECT TOP 1 1 FROM " + TABLE_CREDENTIALS
            + " AS C WHERE (C.username=?) UNION SELECT TOP 1 1 FROM "
            + TABLE_TEMP_CREDENTIALS + " AS tc WHERE (TC.username=?);";
    private static final String CREATE_TEMPORARY_ACCOUNT = "INSERT INTO " + TABLE_TEMP_CREDENTIALS + " (username, created) VALUES (?, ?)";
    private static final String PURGE_TEMPORARY_ACCOUNTS = "DELETE FROM " + TABLE_TEMP_CREDENTIALS + " WHERE (created < ?)";

    // private static final String CREATE_USER_CREDS = "INSERT INTO credentials (username, dpassword) VALUES (?, ?)"; // Auto-generated
    private static final String CREATE_USER_ROLE_AUTH = "INSERT INTO " + TABLE_ROLES + " (username, user_role) VALUES (?, '" + DEFAULT_ROLE + "')";
    // private static final String CREATE_EMPTY_USER = "INSERT INTO " + TABLE_USERS + " (username, fullname, email) VALUES (?, '', '')"; // Auto-generated


    private Supplier<Connection> cSource;
    private UserDAO userDAO;
    private CredentialsDAO credsDAO;

    @Override
    public UserDAO getUserDAO() {
        synchronized (this) {
            if (userDAO == null) userDAO = new UserDAO() {
                @Override
                public @Nullable User getUser(long id) {
                    return getUserByAnyKey(GET_USER_BY_ID, id);
                }

                @Override
                public @Nullable User getUser(String username) {
                    return getUserByAnyKey(GET_USER_BY_LOGIN_NAME, username);
                }

                private User getUserByAnyKey(String sql, Object key) {
                    try (Connection conn = cSource.get(); PreparedStatement st = conn.prepareStatement(sql)) {
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
            };

            return userDAO;
        }
    }

    @Override
    public CredentialsDAO getCredentialsDAO() {
        synchronized (this) {
            if (credsDAO == null) credsDAO = new CredentialsDAO() {
                private volatile boolean saltedHash = false;

                @Override
                public Credentials getCredentials(String username) {
                    String lcName = username.toLowerCase();
                    try (Connection conn = cSource.get(); PreparedStatement st = conn.prepareStatement(GET_CREDS_BY_LOGIN_NAME)) {
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
                public boolean checkIfUserExists(String username) {
                    try (Connection conn = cSource.get(); PreparedStatement st = conn.prepareStatement(CHECK_IF_USER_EXISTS)) {
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
                    try (Connection conn = cSource.get(); PreparedStatement st = conn.prepareStatement(CREATE_TEMPORARY_ACCOUNT)) {
                        st.setString(1, lcName);
                        st.setLong(2, System.currentTimeMillis());
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
                    try (Connection conn = cSource.get(); PreparedStatement st = conn.prepareStatement(PURGE_TEMPORARY_ACCOUNTS)) {
                        st.setLong(1, timeThreshold);
                        log.trace("Executing query: {} <== ({})", PURGE_TEMPORARY_ACCOUNTS, timeThreshold);
                        int numPurged = st.executeUpdate();
                        log.debug("Purged {} temporary user accounts", numPurged);
                    } catch (SQLException e) {
                        log.error("Error purging temporary user accounts: {}", e);
                    }
                }

                @Override
                public Credentials storeNewCredentials(String username, String password) {
                    try (Connection conn = cSource.get()) {
                        conn.setAutoCommit(false);
                        try {
                            Credentials creds = new Credentials(username, password, saltedHash).applyHash();
                            PreparedStatement createCreds = conn.prepareStatement(creds.generateInsertSQL(TABLE_CREDENTIALS));
                            creds.packIntoPreparedStatement(createCreds);
                            log.trace("Saving credentials for user {}", username);
                            if (createCreds.executeUpdate() != 1) throw new SQLException("Error storing credentials for user " + username);

                            PreparedStatement createRole = conn.prepareStatement(CREATE_USER_ROLE_AUTH);
                            createRole.setString(1, username);
                            log.trace("Setting role for user {}", username);
                            if (createRole.executeUpdate() != 1) throw new SQLException("Error assigning role for user " + username);

                            User emptyUser = new User(username, username, "", false);
                            PreparedStatement createUser = conn.prepareStatement(emptyUser.generateInsertSQL(TABLE_USERS));
                            emptyUser.packIntoPreparedStatement(createUser);
                            log.trace("Creating main record for user {}", username);
                            if (createUser.executeUpdate() != 1) throw new SQLException("Error creating main user record for user " + username);

                            conn.commit();
                            return creds;
                        } catch (SQLException e) {
                            conn.rollback();
                            throw e;
                        }
                    } catch (SQLException e) {
                        log.error("Error creating user '{}': {}", username, e);
                        return null;
                    }
                }
            };

            return credsDAO;
        }
    }

    @Override
    public void useConnectionSource(Supplier<Connection> src) {
        synchronized (this) {
            credsDAO = null;
            userDAO = null;
            cSource = src;
        }
    }
}
