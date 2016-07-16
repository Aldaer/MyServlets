package model.dao;

import dbconnecton.ConnectionPool;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static com.aldor.utils.CryptoUtils.verifySaltedHash;
import static model.dao.StandardDAO.*;

/**
 * Simple user database implementation using h2
 */
@Log4j2
public class UserDaoH2 implements UserDAO {
    private static final ConnectionPool cp;

    private static final String GET_USER_BY_NAME = "SELECT ID, USERNAME, EMAIL, DPASSWORD FROM USERS WHERE (USERNAME=?)";
    private static final String GET_USER_BY_ID = "SELECT ID, USERNAME, EMAIL, DPASSWORD FROM USERS WHERE (ID=?)";
    private static final boolean USING_SALTED_DIGEST;

    static {
        ResourceBundle conf = ResourceBundle.getBundle("config");
        String uri = conf.getString(CONFIG_DATABASE_URI);
        String drv = conf.getString(CONFIG_DATABASE_DRIVER);
        log.info("Creating connection pool with driver {}, uri {}", drv, uri);
        String un = conf.getString(CONFIG_DATABASE_USER);
        String pwd = conf.getString(CONFIG_DATABASE_PASSWORD);
        cp = ConnectionPool.builder()
                .withDriver(drv)
                .withUrl(uri)
                .withUserName(un)
                .withPassword(pwd)
                .withLogger(LogManager.getLogger(ConnectionPool.class))
                .create();

        USING_SALTED_DIGEST = Boolean.valueOf(conf.getString(CONFIG_DATABASE_USE_SHA_DIGEST));
    }

    @Override
    public User getUser(String username) {
        try (Connection conn = cp.getConnection()) {
            PreparedStatement st = conn.prepareStatement(GET_USER_BY_NAME);
            st.setObject(1, username);
            log.trace(() -> "Executing query: " + GET_USER_BY_NAME + " VALUES (" + username + ")");
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
        try (Connection conn = cp.getConnection()) {
            PreparedStatement st = conn.prepareStatement(GET_USER_BY_ID);
            st.setObject(1, id);
            log.trace(() -> "Executing query: " + GET_USER_BY_ID + " VALUES (" + id + ")");
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
    public boolean authenticateUser(User user, String password) {
        if (user == null) return false;

        if (USING_SALTED_DIGEST) return verifySaltedHash(user.getDPassword(), password);
        else return user.getDPassword().equals(password);
    }
}
