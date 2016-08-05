package model.dao.databases;

import lombok.extern.slf4j.Slf4j;
import model.dao.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.sql.ResultSet.*;
import static java.util.Optional.ofNullable;
import static model.dao.databases.GenericSqlDAO.*;
import static model.dao.databases.Stored.Processor.getColumnForField;

/**
 * Global DAO for SQL databases. H2 and MySQL modes are currently supported. Since this class has no nullary constructor,
 * use wrappers if instantiation with newInstance() is necessary.
 * Actual User and Credentials dao returned by this Global DAO are singletons created with a connection source
 * configured in GlobalDAO at the time they are requested. Changing connection source in GlobalDAO does not affect
 * any existing instances of User and Credentials dao, but next request for User and Credentials dao will create new instances.
 * Automatic commit on {@code Connection.close()} expected.
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
public class GenericSqlDAO implements GlobalDAO, DatabaseDAO {
    final String upsertPrefix;
    final String commentToken;
    
    static final String TABLE_USERS = "users";
    static final String TABLE_CREDENTIALS = "credentials";
    static final String TABLE_TEMP_CREDENTIALS = "temp_credentials";
    static final String TABLE_ROLES = "user_roles";
    static final String DEFAULT_ROLE = "authenticated-user";
    static final String TABLE_MESSAGES = "messages";
    static final String TABLE_FRIENDS = "friends";


    // Columns in tables with their own DAO classes
    static final String CFF_CR_UNAME = getColumnForField(Credentials.class, "uName");
    static final String CFF_CR_PWD = getColumnForField(Credentials.class, "pwd");

    static final String CFF_USR_ID = getColumnForField(User.class, "id");
    static final String CFF_USR_UNAME = getColumnForField(User.class, "username");
    static final String CFF_USR_FNAME = getColumnForField(User.class, "fullName");

    static final String CFF_MES_ID = getColumnForField(Message.class, "id");
    static final String CFF_MES_REF = getColumnForField(Message.class, "refId");
    static final String CFF_MES_FROM = getColumnForField(Message.class, "from");
    static final String CFF_MES_TO = getColumnForField(Message.class, "to");
    static final String CFF_MES_TIME = getColumnForField(Message.class, "utcTimestamp");
    static final String CFF_MES_TEXT = getColumnForField(Message.class, "text");
    static final String CFF_MES_CONV = getColumnForField(Message.class, "conversationId");

    // Columns in tables with no DAO classes
    // Temporary credentials
    static final String COL_TCR_UNAME = "username";
    static final String COL_TCR_CRT = "created";
    // User roles
    static final String COL_RLS_UNAME = "username";
    static final String COL_RLS_UROLE = "user_role";
    // Friends
    static final String COL_FRN_UID = "uid";
    static final String COL_FRN_FID = "fid";


    static final String GET_CREDS_BY_LOGIN_NAME = "SELECT " + CFF_CR_PWD + " FROM " + TABLE_CREDENTIALS + " WHERE (" + CFF_CR_UNAME + "=?);";
    static final String GET_USER_BY_LOGIN_NAME = "SELECT * FROM " + TABLE_USERS + " WHERE (" + CFF_USR_UNAME + "=?);";
    static final String GET_USER_BY_ID = "SELECT * FROM " + TABLE_USERS + " WHERE (" + CFF_USR_ID + "=?);";

    static final String GET_USER_BY_PARTIAL_NAME = "SELECT " + CFF_USR_ID + "," + CFF_USR_UNAME + "," + CFF_USR_FNAME 
            + " FROM " + TABLE_USERS + " WHERE (" + CFF_USR_UNAME + " LIKE ? OR " + CFF_USR_FNAME + " LIKE ?) LIMIT ";
    static final int MIN_PARTIAL_LEN = 2;

    static final String CHECK_IF_USER_EXISTS = "SELECT 1 FROM " + TABLE_CREDENTIALS
            + " AS C WHERE (C." + CFF_CR_UNAME + "=?) UNION SELECT 1 FROM "
            + TABLE_TEMP_CREDENTIALS + " AS TC WHERE (TC." + COL_TCR_UNAME + "=?);";

    static final String CREATE_TEMPORARY_ACCOUNT = "INSERT INTO " + TABLE_TEMP_CREDENTIALS 
            + " (" + COL_TCR_UNAME + ", " + COL_TCR_CRT + ") VALUES (?, ?);";
    static final String PURGE_TEMPORARY_ACCOUNTS = "DELETE FROM " + TABLE_TEMP_CREDENTIALS 
            + " WHERE (" + COL_TCR_CRT + " < ?);";

    // Default ResultSet behaviour: ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY
    static final String CREATE_USER_ROLE_AUTH = "INSERT INTO " + TABLE_ROLES + " (" + COL_RLS_UNAME + ", " 
            + COL_RLS_UROLE + ") VALUES (?, '" + DEFAULT_ROLE + "');";

    static final String GET_USER_FOR_UPDATE = "SELECT * FROM " + TABLE_USERS + " WHERE (username=?) FOR UPDATE;";

    static final String GET_USERS_FRIEND_IDS = "SELECT fid FROM " + TABLE_FRIENDS + " WHERE (" + COL_FRN_UID + "=?);";
    static final String GET_USERS_FRIEND_DETAILS = "SELECT * FROM " + TABLE_USERS + " INNER JOIN " + TABLE_FRIENDS 
            + " ON " + TABLE_USERS + "." + CFF_USR_ID + " = " + TABLE_FRIENDS + "." + COL_FRN_FID 
            + " WHERE " + TABLE_FRIENDS + "." + COL_FRN_UID + "=";

    static final String ADD_FRIEND_POSTFIX = " INTO " + TABLE_FRIENDS + " (" + COL_FRN_UID + ", " + COL_FRN_FID + ") VALUES (?,?);";

    static final String REMOVE_FRIEND = "DELETE FROM " + TABLE_FRIENDS + " WHERE (" + COL_FRN_UID + "=? AND " + COL_FRN_FID + "=?);";

    static final String DELETE_MESSAGE_QUERY = "DELETE FROM " + TABLE_MESSAGES + " WHERE " + CFF_MES_ID + "=";

    static final String WRONG_ROW_COUNT = "Wrong affected row count";

    private Supplier<Connection> cSource;
    private UserDAO userDAO;
    private CredentialsDAO credsDAO;
    private MessageDAO messDAO;

    public GenericSqlDAO(SqlMode compatibility) {
        switch (compatibility) {
            case MY_SQL:
                upsertPrefix = "INSERT IGNORE";
                commentToken = "#";
                break;
            case H2:
                upsertPrefix = "MERGE";
                commentToken = "//";
                break;
            default:
                upsertPrefix = "*UNSUPPORTED*";
                commentToken = "*UNSUPPORTED*";
        }
    }
    
    enum SqlMode { MY_SQL, H2 }
    
    @Override
    public UserDAO getUserDAO() {
        synchronized (this) {
            if (userDAO == null) userDAO = new SqlUserDAO(cSource, upsertPrefix);
            return userDAO;
        }
    }

    @Override
    public CredentialsDAO getCredentialsDAO() {
        synchronized (this) {
            if (credsDAO == null) credsDAO = new SqlCredentialsDAO(cSource);
            return credsDAO;
        }
    }

    @Override
    public MessageDAO getMessageDAO() {
        synchronized (this) {
            if (messDAO == null) messDAO = new SqlMessageDAO(cSource);
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
            for (String line : script) {
                line = line.trim();
                if (line.startsWith(commentToken)) continue;
                int pos = 0;
                int semiC;
                while ((semiC = line.indexOf(";", pos)) >= 0) {
                    String fragment = line.substring(pos, semiC + 1);
                    currentCmd.append(fragment);
                    pos = semiC + 1;
                    literalMode = literalMode ^ fragment.contains("$$");
                    if (!literalMode) try {
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
class SqlUserDAO implements UserDAO {
    private final Supplier<Connection> cSource;

    private final String addFriendQuery;

    SqlUserDAO(Supplier<Connection> cSource, String upsertPrefix) {
        this.cSource = cSource;
        addFriendQuery = upsertPrefix + ADD_FRIEND_POSTFIX;
    }

    @Override
    public @Nullable User getUser(long id) {
        return getUserByAnyKey(GET_USER_BY_ID, id);
    }

    @Override
    public @Nullable User getUser(String username) {
        return (username == null) ? null : getUserByAnyKey(GET_USER_BY_LOGIN_NAME, username);
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
    public Collection<ShortUserInfo> listUsers(@Nullable String partialName, int limit) {
        if (partialName == null || partialName.length() < MIN_PARTIAL_LEN) return Collections.emptyList();
        String sql = GET_USER_BY_PARTIAL_NAME + limit + ";";

        try (Connection conn = cSource.get();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            partialName = "%" + partialName + "%";
            pst.setString(1, partialName);
            pst.setString(2, partialName);
            log.trace("Executing query: {}{} <== ({})", GET_USER_BY_PARTIAL_NAME, limit, partialName);
            ResultSet rs = pst.executeQuery();
            return reconstructShortUserInfo(rs);
        } catch (SQLException e) {
            log.error("Error getting list of users: {}", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Collection<ShortUserInfo> listFriends(long userId) {
        String sql = GET_USERS_FRIEND_DETAILS + userId + ";";
        try (Connection conn = cSource.get();
             Statement st = conn.createStatement()) {
            log.trace("Executing query: {}", sql);
            ResultSet rs = st.executeQuery(sql);
            return reconstructShortUserInfo(rs);
        } catch (SQLException e) {
            log.error("Error getting list of friends for user #{}: {}", userId, e);
            return Collections.emptyList();
        }
    }

    private Collection<ShortUserInfo> reconstructShortUserInfo(ResultSet rs) throws SQLException {
        List<User> tmpList = new ArrayList<>();
        Stored.Processor.reconstructAllObjects(rs, () -> new User("", "", "", true), tmpList, true);
        return tmpList.stream().map(User::shortInfo).collect(Collectors.toCollection(ArrayList::new));
    }


    @Override
    public @NotNull long[] getFriendIds(long id) {
        int currSize = 20;
        long[] result = new long[currSize];
        int n = 0;
        try (Connection conn = cSource.get();
             PreparedStatement pst = conn.prepareStatement(GET_USERS_FRIEND_IDS)) {
            pst.setLong(1, id);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                long fid = rs.getLong(1);
                if (n >= currSize) {
                    currSize *= 2;
                    result = Arrays.copyOf(result, currSize);
                }
                result[n++] = fid;
            }

        } catch (SQLException e) {
            log.error("Error getting list of friends for user id={}: {}", id, e);
        }
        return Arrays.copyOf(result, n);
    }

    @Override
    public void addFriend(long id, Long friendId) {
        if (friendId == null) return;
        try (Connection conn = cSource.get();
             PreparedStatement pst = conn.prepareStatement(addFriendQuery)) {
            pst.setLong(1, id);
            pst.setLong(2, friendId);
            log.trace("Executing query: {} <== ({},{})", addFriendQuery, id, friendId);
            pst.executeUpdate();
//            if (pst.executeUpdate() != 1) throw new SQLException(WRONG_ROW_COUNT);        // H2 only
        } catch (SQLException e) {
            log.error("Error adding friend #{} to user #{}'s list: {}", friendId, id, e);
        }
    }

    @Override
    public void removeFriend(long id, Long friendId) {
        if (friendId == null) return;
        try (Connection conn = cSource.get();
             PreparedStatement pst = conn.prepareStatement(REMOVE_FRIEND)) {
            pst.setLong(1, id);
            pst.setLong(2, friendId);
            log.trace("Executing query: {} <== ({},{})", REMOVE_FRIEND, id, friendId);
            if (pst.executeUpdate() != 1) throw new SQLException(WRONG_ROW_COUNT);
        } catch (SQLException e) {
            log.error("Error removing friend #{} from user #{}'s list: {}", friendId, id, e);
        }

    }
}

@Slf4j
class SqlCredentialsDAO implements CredentialsDAO {
    private volatile boolean saltedHash = false;

    SqlCredentialsDAO(Supplier<Connection> cSource) {
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
            if (pst.executeUpdate() != 1) throw new SQLException(WRONG_ROW_COUNT);
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
class SqlMessageDAO implements MessageDAO {
    SqlMessageDAO(Supplier<Connection> cSource) {
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

    @Override
    public void sendMessage(Message message) {
        String query = message.generateInsertSQL(TABLE_MESSAGES);
        try (Connection conn = cSource.get();
             PreparedStatement pst = conn.prepareStatement(query)) {
            message.packIntoPreparedStatement(pst);
            log.trace("Sending new message from {}", message.getFrom());
            if (pst.executeUpdate() != 1)
                throw new SQLException("Wrong update count");
        } catch (SQLException e) {
            log.error("Error sending message from '{}' [{}]: {}", message.getFrom(), message.getText(), e);
        }
    }

    @Override
    public void updateMessage(long id, String newText, Boolean unread) {
        StringBuilder sqlB = new StringBuilder(250);
        sqlB.append("UPDATE ").append(TABLE_MESSAGES).append(" SET ");
        Object[] params = new Object[2];
        int nPar = 0;
        if (newText != null) {
            sqlB.append(CFF_MES_TEXT).append("=?,");
            params[nPar++] = newText;
        }
        if (unread != null) {
            sqlB.append(CFF_MES_CONV).append("=?,");
            params[nPar++] = unread ? 0 : -1;
        }
        if (nPar > 0) sqlB.setLength(sqlB.length() - 1);
        else return;

        sqlB.append(" WHERE ")
                .append(CFF_MES_ID)
                .append("=")
                .append(id)
                .append(';');

        String stmt = sqlB.toString();

        try (Connection conn = cSource.get();
             PreparedStatement pst = conn.prepareStatement(stmt,
                     TYPE_FORWARD_ONLY, CONCUR_UPDATABLE, CLOSE_CURSORS_AT_COMMIT)) {

            for (int j = 0; j < nPar; j++)
                pst.setObject(j + 1, params[j]);

            log.trace("Executing query: {}", stmt);

            if (pst.executeUpdate() != 1) throw new SQLException("Invalid update count");
        } catch (SQLException e) {
            log.error("Error updating data for message [{}]: {}", id, e);
        }
    }

    @Override
    public void deleteMessage(long id) {
        try (Connection conn = cSource.get(); Statement st = conn.createStatement()) {
            int nDeleted = st.executeUpdate(DELETE_MESSAGE_QUERY + id + ";");
            log.trace("Deleted {} message(s)", nDeleted);
        } catch (SQLException e) {
            log.error("Error deleting message #{}: {}", id, e);
        }
    }

    private String buildMessageQuery(String prefix, MessageFilter constraint, boolean countOnly) {
        StringBuilder sqlB = new StringBuilder(250);
        sqlB.append(prefix).append(TABLE_MESSAGES);

        List<String> constraintList = new ArrayList<>(10);
        ofNullable(constraint.getId()).map(id -> CFF_MES_ID + "=" + id).ifPresent(constraintList::add);
        ofNullable(constraint.getRefId()).map(refid -> CFF_MES_REF + "=" + refid).ifPresent(constraintList::add);
        ofNullable(constraint.getMinTime()).map(mt -> CFF_MES_TIME + ">='" + mt + "'").ifPresent(constraintList::add);
        ofNullable(constraint.getMaxTime()).map(mt -> CFF_MES_TIME + "<='" + mt + "'").ifPresent(constraintList::add);
        ofNullable(constraint.getTextLike()).map(txt -> CFF_MES_TEXT + " LIKE ?").ifPresent(constraintList::add);

        Long[] convId = constraint.getConvId();
        if (convId != null && convId.length > 0) {
            StringBuilder convs = new StringBuilder(50)
                    .append("(").append(CFF_MES_CONV)
                    .append("=").append(convId[0]);
            for (int i = 1; i < convId.length; i++)
                convs.append(" OR ").append(CFF_MES_CONV)
                        .append("=").append(convId[i]);
            convs.append(")");
            constraintList.add(convs.toString());
        }

        // FROM user1 TO user1 means FROM OR TO, not FROM AND TO
        if (constraint.getFrom() != null && constraint.getTo() != null && constraint.getFrom().equals(constraint.getTo())) {
            constraintList.add("(" + CFF_MES_FROM + "='" + constraint.getFrom() + "' OR " +
                    CFF_MES_TO + "='" + constraint.getTo() + "')");
        } else {
            ofNullable(constraint.getFrom()).map(from -> CFF_MES_FROM + "='" + from + "'").ifPresent(constraintList::add);
            ofNullable(constraint.getTo()).map(to -> CFF_MES_TO + "='" + to + "'").ifPresent(constraintList::add);
        }

        if (constraintList.size() > 0) {
            sqlB.append(" WHERE ").append(constraintList.get(0));
            for (int i = 1; i < constraintList.size(); i++)
                sqlB.append(" AND ").append(constraintList.get(i));
        }

        if (!countOnly) {
            ofNullable(constraint.getLimit()).map(lim -> " LIMIT " + lim).ifPresent(sqlB::append);
            ofNullable(constraint.getOffset()).map(offs -> " OFFSET " + offs).map(offs ->                   // MySql patch: Can't use OFFSET without LIMIT
                constraint.getLimit()==null? " LIMIT " + Integer.MAX_VALUE + offs : offs ).ifPresent(sqlB::append);
            ofNullable(constraint.getSortField()).map(srtf -> " ORDER BY " + getColumnForField(Message.class, srtf)).ifPresent(constraintList::add);
        }

        sqlB.append(';');
        return sqlB.toString();
    }
}
