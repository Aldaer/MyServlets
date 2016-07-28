package model.dao.databases;

import lombok.extern.slf4j.Slf4j;
import model.dao.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.sql.ResultSet.*;
import static java.util.Optional.ofNullable;
import static model.dao.databases.H2GlobalDAO.*;
import static model.dao.databases.Stored.Processor.getColumnForField;

/**
 * Global DAO for H2 database.
 * Actual User and Credentials dao returned by this Global DAO are singletons created with a connection source
 * configured in GlobalDAO at the time they are requested. Changing connection source in GlobalDAO does not affect
 * any existing instances of User and Credentials dao, but next request for User and Credentials dao will create new instances.
 * Automatic commit on {@code Connection.close()} expected.
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
public class H2GlobalDAO implements GlobalDAO, DatabaseDAO {
    static final String TABLE_USERS = "users";
    static final String TABLE_CREDENTIALS = "credentials";
    static final String TABLE_TEMP_CREDENTIALS = "temp_credentials";
    static final String TABLE_ROLES = "user_roles";
    static final String DEFAULT_ROLE = "authenticated-user";
    static final String TABLE_MESSAGES = "messages";

    static final String GET_CREDS_BY_LOGIN_NAME = "SELECT dpassword FROM " + TABLE_CREDENTIALS + " WHERE (username=?);";
    static final String GET_USER_BY_LOGIN_NAME = "SELECT * FROM " + TABLE_USERS + " WHERE (username=?);";
    static final String GET_USER_BY_ID = "SELECT * FROM " + TABLE_USERS + " WHERE (id=?);";

    static final String GET_USER_BY_PARTIAL_NAME = "SELECT username, fullname FROM " + TABLE_USERS + " WHERE (username LIKE ? OR fullname LIKE ?) LIMIT ";
    static final int MIN_PARTIAL_LEN = 2;

    static final String CHECK_IF_USER_EXISTS = "SELECT TOP 1 1 FROM " + TABLE_CREDENTIALS
            + " AS C WHERE (C.username=?) UNION SELECT TOP 1 1 FROM "
            + TABLE_TEMP_CREDENTIALS + " AS tc WHERE (TC.username=?);";
    static final String CREATE_TEMPORARY_ACCOUNT = "INSERT INTO " + TABLE_TEMP_CREDENTIALS + " (username, created) VALUES (?, ?);";
    static final String PURGE_TEMPORARY_ACCOUNTS = "DELETE FROM " + TABLE_TEMP_CREDENTIALS + " WHERE (created < ?);";

    // Default ResultSet behaviour: ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY

    // private static final String CREATE_USER_CREDS = "INSERT INTO credentials (username, dpassword) VALUES (?, ?)"; // Auto-generated
    // private static final String CREATE_EMPTY_USER = "INSERT INTO " + TABLE_USERS + " (username, fullname, email) VALUES (?, '', '')"; // Auto-generated
    static final String CREATE_USER_ROLE_AUTH = "INSERT INTO " + TABLE_ROLES + " (username, user_role) VALUES (?, '" + DEFAULT_ROLE + "');";

    static final String GET_USER_FOR_UPDATE = "SELECT * FROM " + TABLE_USERS + " WHERE (username=?) FOR UPDATE;";

    private Supplier<Connection> cSource;
    private UserDAO userDAO;
    private CredentialsDAO credsDAO;
    private MessageDAO messDAO;

    @Override
    public UserDAO getUserDAO() {
        synchronized (this) {
            if (userDAO == null) userDAO = new H2UserDAO(cSource);
            return userDAO;
        }
    }

    @Override
    public CredentialsDAO getCredentialsDAO() {
        synchronized (this) {
            if (credsDAO == null) credsDAO = new H2CredentialsDAO(cSource);
            return credsDAO;
        }
    }

    @Override
    public MessageDAO getMessageDAO() {
        synchronized (this) {
            if (messDAO == null) messDAO = new H2MessageDAO(cSource);
            return messDAO;
        }
    }

    @Override
    public void useConnectionSource(Supplier<Connection> src) {
        synchronized (this) {
            credsDAO = null;
            userDAO = null;
            messDAO = null;
            cSource = src;
        }
    }

    public void executeScript(String[] script) {
        try (Connection connection = cSource.get();
            Statement statement = connection.createStatement()) {

            StringBuilder currentCmd = new StringBuilder();
            boolean literalMode = false;
            for (String line: script) {
                line = line.trim();
                if (line.startsWith("//")) continue;
                int pos = 0;
                int semiC;
                while ((semiC = line.indexOf(";", pos)) >= 0) {
                    String fragment = line.substring(pos, semiC + 1);
                    currentCmd.append(fragment);
                    pos = semiC + 1;
                    literalMode = literalMode ^ fragment.contains("$$");
                    if (! literalMode) try {
                        String cmd = currentCmd.toString();
                        statement.addBatch(cmd);
                        log.trace("Added command to batch: {}", cmd);
                    } catch (SQLException e) {
                        log.error("Error adding SQL command {} to batch: {}", currentCmd.toString(), e);
                    } finally {
                        currentCmd.setLength(0);
                    }
                }
                if (pos < line.length()) {
                    String tail = line.substring(pos);
                    literalMode = literalMode ^ tail.contains("$$");
                    currentCmd.append(tail).append(" ");
                }
            }
            int[] results = statement.executeBatch();
            log.trace("Executed batch of {} commands", results.length);
        } catch (SQLException e) {
            log.error("Error executing SQL script: {}", e);
        }
    }
}

@Slf4j
class H2UserDAO implements UserDAO {
    private final Supplier<Connection> cSource;

    H2UserDAO(Supplier<Connection> cSource) {
        this.cSource = cSource;
    }

    @Override
    public @Nullable User getUser(long id) {
        return getUserByAnyKey(GET_USER_BY_ID, id);
    }

    @Override
    public @Nullable User getUser(String username) {
        return (username == null)? null : getUserByAnyKey(GET_USER_BY_LOGIN_NAME, username);
    }

    private User getUserByAnyKey(String sql, Object key) {
        try (Connection conn = cSource.get();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setObject(1, key);
            log.trace("Executing query: {} <== ({})", sql, key);
            ResultSet rs = pst.executeQuery();
            if (!rs.next()) {
                log.trace("User '{}' not found", key);
                return null;
            }
            return Stored.Processor.reconstructObject(rs, User::new);
        } catch (SQLException e) {
            log.error("Error getting data for user [{}]: {}", key, e);
            return null;
        }
    }

    @Override
    public void updateUserInfo(@NotNull User user) {
        try (Connection conn = cSource.get();
             PreparedStatement pst = conn.prepareStatement(GET_USER_FOR_UPDATE,
                     TYPE_FORWARD_ONLY, CONCUR_UPDATABLE, CLOSE_CURSORS_AT_COMMIT)) {
            pst.setString(1, user.getUsername());
            log.trace("Executing query: {} <== ({})", GET_USER_FOR_UPDATE, user.getUsername());
            ResultSet rs = pst.executeQuery();
            if (!rs.next()) throw new SQLException("User not found");
            user.injectIntoResultSet(rs);
            rs.updateRow();
            rs.close();
        } catch (SQLException e) {
            log.error("Error updating data for user [{}]: {}", user.getUsername(), e);
        }
    }

    @Override
    public Map<String, String> listUsers(@Nullable String partialName, int limit) {
        Map<String, String> emptyMap = new HashMap<>(0);
        if (partialName == null || partialName.length() < MIN_PARTIAL_LEN) return emptyMap;
        String sql = GET_USER_BY_PARTIAL_NAME + limit + ";";

        try (Connection conn = cSource.get();
            PreparedStatement pst = conn.prepareStatement(sql)) {
            partialName = "%" + partialName + "%";
            pst.setString(1, partialName);
            pst.setString(2, partialName);
            log.trace("Executing query: {}{} <== ({})", GET_USER_BY_PARTIAL_NAME, limit, partialName);
            ResultSet rs = pst.executeQuery();
            List<User> tmpList = new ArrayList<>();
            Stored.Processor.reconstructAllObjects(rs, () -> new User("", "", "", true), tmpList, true);
            return tmpList.stream().collect(Collectors.toMap(User::getUsername, User::getFullName));
        } catch (SQLException e) {
            log.error("Error getting list of users: {}", e);
            return emptyMap;
        }
    }
}

@Slf4j
class H2CredentialsDAO implements CredentialsDAO {
    private volatile boolean saltedHash = false;

    H2CredentialsDAO(Supplier<Connection> cSource) {
        this.cSource = cSource;
    }

    private final Supplier<Connection> cSource;

    @Override
    public Credentials getCredentials(String login) {
        String lcName = login.toLowerCase();
        try (Connection conn = cSource.get();
             PreparedStatement pst = conn.prepareStatement(GET_CREDS_BY_LOGIN_NAME)) {
            pst.setObject(1, lcName);
            log.trace("Executing query: {} <== ({})", GET_CREDS_BY_LOGIN_NAME, lcName);
            ResultSet rs = pst.executeQuery();
            if (!rs.next()) {
                log.trace("User '{}' not found", login);
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
    public boolean checkIfLoginOccupied(String login) {
        try (Connection conn = cSource.get();
             PreparedStatement pst = conn.prepareStatement(CHECK_IF_USER_EXISTS)) {
            pst.setString(1, login);
            pst.setString(2, login);
            log.trace("Executing query: {} <== ({}, {})", CHECK_IF_USER_EXISTS, login, login);
            return pst.executeQuery().next();
        } catch (SQLException e) {
            log.error("Error checking if user {} exists: {}", login, e);
            return false;
        }
    }

    @Override
    public boolean createTemporaryUser(String login) {
        String lcName = login.toLowerCase();
        try (Connection conn = cSource.get();
             PreparedStatement pst = conn.prepareStatement(CREATE_TEMPORARY_ACCOUNT)) {
            pst.setString(1, lcName);
            pst.setLong(2, System.currentTimeMillis());
            log.trace("Executing query: {} <== ({})", CREATE_TEMPORARY_ACCOUNT, login);
            if (pst.executeUpdate() != 1) throw new SQLException("Wrong affected row count");
        } catch (SQLException e) {
            log.error("Error creating temporary account for user: {}", login);
            return false;
        }
        return true;
    }

    @Override
    public void purgeTemporaryUsers(long timeThreshold) {
        try (Connection conn = cSource.get(); PreparedStatement pst = conn.prepareStatement(PURGE_TEMPORARY_ACCOUNTS)) {
            pst.setLong(1, timeThreshold);
            log.trace("Executing query: {} <== ({})", PURGE_TEMPORARY_ACCOUNTS, timeThreshold);
            int numPurged = pst.executeUpdate();
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
                if (createCreds.executeUpdate() != 1)
                    throw new SQLException("Error storing credentials for user " + username);
                createCreds.close();

                PreparedStatement createRole = conn.prepareStatement(CREATE_USER_ROLE_AUTH);
                createRole.setString(1, username);
                log.trace("Setting role for user {}", username);
                if (createRole.executeUpdate() != 1)
                    throw new SQLException("Error assigning role for user " + username);
                createRole.close();

                User emptyUser = new User(username, username, "", false);
                PreparedStatement createUser = conn.prepareStatement(emptyUser.generateInsertSQL(TABLE_USERS));
                emptyUser.packIntoPreparedStatement(createUser);
                log.trace("Creating main record for user {}", username);
                if (createUser.executeUpdate() != 1)
                    throw new SQLException("Error creating main user record for user " + username);
                createUser.close();

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
}

@Slf4j
class H2MessageDAO implements MessageDAO {
    H2MessageDAO(Supplier<Connection> cSource) {
        this.cSource = cSource;
    }

    private final Supplier<Connection> cSource;

    @Override
    public List<Message> getMessages(MessageFilter constraint) {
        String query = buildMessageQuery("SELECT * FROM ", constraint, false);

        try (Connection conn = cSource.get(); PreparedStatement pst = conn.prepareStatement(query)) {
            String textSearch = constraint.getTextLike();
            if (textSearch != null) pst.setString(1, textSearch);
            log.trace("Executing query: {}", query);
            ResultSet rs = pst.executeQuery();
            List<Message> lm = new ArrayList<>();
            Stored.Processor.reconstructAllObjects(rs, Message::new, lm, false);
            return lm;
        } catch (SQLException e) {
            log.error("Error getting data from table '{}': {}", TABLE_MESSAGES, e);
            return new ArrayList<>();
        }
    }

    @Override
    public int countMessages(MessageFilter constraint) {
        String query = buildMessageQuery("SELECT COUNT(*) AS mcount FROM ", constraint, true);

        try (Connection conn = cSource.get(); PreparedStatement pst = conn.prepareStatement(query)) {
            String textSearch = constraint.getTextLike();
            if (textSearch != null) pst.setString(1, textSearch);
            log.trace("Executing query: {}", query);
            ResultSet rs = pst.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            log.error("Error counting rows in table '{}': {}", TABLE_MESSAGES, e);
            return -1;
        }
    }

    private String buildMessageQuery(String prefix, MessageFilter constraint, boolean countOnly) {
        StringBuilder sqlB = new StringBuilder(250);
        sqlB.append(prefix).append(TABLE_MESSAGES);

        List<String> constraintList = new ArrayList<>(10);
        ofNullable(constraint.getId()).map(id -> getColumnForField(Message.class, "id") + "=" + id).ifPresent(constraintList::add);
        ofNullable(constraint.getRefId()).map(refid -> getColumnForField(Message.class, "refId") + "=" + refid).ifPresent(constraintList::add);
        ofNullable(constraint.getMinTime()).map(mt -> getColumnForField(Message.class, "utcTimestamp") + ">='" + mt + "'").ifPresent(constraintList::add);
        ofNullable(constraint.getMaxTime()).map(mt -> getColumnForField(Message.class, "utcTimestamp") + "<='" + mt + "'").ifPresent(constraintList::add);
        ofNullable(constraint.getConvId()).map(convid -> getColumnForField(Message.class, "conversationId") + "=" + convid).ifPresent(constraintList::add);
        ofNullable(constraint.getTextLike()).map(txt -> getColumnForField(Message.class, "text") + " LIKE ?").ifPresent(constraintList::add);

        // FROM user1 TO user1 means FROM OR TO, not FROM AND TO
        if (constraint.getFrom() != null && constraint.getTo() != null && constraint.getFrom().equals(constraint.getTo())) {
            constraintList.add("(" + getColumnForField(Message.class, "from") + "='" + constraint.getFrom() + "' OR " +
                    getColumnForField(Message.class, "to") + "='" + constraint.getTo() + "')");
        } else {
            ofNullable(constraint.getFrom()).map(from -> getColumnForField(Message.class, "from") + "='" + from + "'").ifPresent(constraintList::add);
            ofNullable(constraint.getTo()).map(to -> getColumnForField(Message.class, "to") + "='" + to + "'").ifPresent(constraintList::add);
        }

        if (constraintList.size() > 0) {
            sqlB.append(" WHERE ").append(constraintList.get(0));
            for (int i = 1; i < constraintList.size(); i++)
                sqlB.append(" AND ").append(constraintList.get(i));
        }

        if (! countOnly) {
            ofNullable(constraint.getLimit()).map(lim -> " LIMIT " + lim).ifPresent(sqlB::append);
            ofNullable(constraint.getOffset()).map(offs -> " OFFSET " + offs).ifPresent(sqlB::append);
            ofNullable(constraint.getSortField()).map(srtf -> " ORDER BY " + getColumnForField(Message.class, srtf)).ifPresent(constraintList::add);
        }

        sqlB.append(';');
        return sqlB.toString();
    }
}