package model.dao;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

import static com.aldor.utils.CryptoUtils.verifySaltedHash;

/**
 * Simple user database implementation using h2
 */
@Slf4j
public class UserDaoH2 implements UserDAO {

    private static final String GET_USER_BY_NAME = "SELECT ID, USERNAME, EMAIL, DPASSWORD FROM USERS WHERE (USERNAME=?)";
    private static final String GET_USER_BY_ID = "SELECT ID, USERNAME, EMAIL, DPASSWORD FROM USERS WHERE (ID=?)";

    private volatile Supplier<Connection> cSource;
    private volatile boolean useSaltedHash;

    @Override
    public User getUser(String username) {
        try (Connection conn = cSource.get()) {
            PreparedStatement st = conn.prepareStatement(GET_USER_BY_NAME);
            st.setObject(1, username);
            log.trace("Executing query: " + GET_USER_BY_NAME + " VALUES (" + username + ")");
            ResultSet rs = st.executeQuery();
            rs.next();

            SimpleUser user = new SimpleUser();
            user.fillFromResultSet(rs);
            return user;

        } catch (SQLException e) {
            log.error("Error getting user id: {}", e);
            return null;
        }
    }

    @Override
    public User getUser(long id) {
        try (Connection conn = cSource.get()) {
            PreparedStatement st = conn.prepareStatement(GET_USER_BY_ID);
            st.setObject(1, id);
            log.trace("Executing query: " + GET_USER_BY_ID + " VALUES (" + id + ")");
            ResultSet rs = st.executeQuery();
            rs.next();
            SimpleUser user = new SimpleUser();
            user.fillFromResultSet(rs);
            return user;
        } catch (SQLException e) {
            log.error("Error getting user by id: {}", e);
            return null;
        }
    }

    @Override
    public void useConnectionSource(Supplier<Connection> src) {
        cSource = src;
    }

    @Override
    public boolean authenticateUser(User user, String password) {
        if (user == null) return false;

        if (useSaltedHash) return verifySaltedHash(user.getDPassword(), password);
        else return user.getDPassword().equals(password);
    }

    @Override
    public void useSaltedHash(boolean doUse) {
        useSaltedHash = doUse;
    }
}
