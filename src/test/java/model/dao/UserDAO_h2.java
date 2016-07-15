package model.dao;

import lombok.extern.log4j.Log4j2;
import model.dbconnecton.ConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

import static model.dao.StandardDAO.CONFIG_DATABASE_DRIVER;
import static model.dao.StandardDAO.CONFIG_DATABASE_URI;

/**
 * Simple user database implementation using h2
 */
@Log4j2
public class UserDAO_h2 implements UserDAO {
    private static final ConnectionPool cp;

    private static final String GET_USER_BY_NAME = "SELECT ID, USERNAME, EMAIL, DPASSWORD FROM USERS WHERE (USERNAME=?)";
    private static final String GET_USER_BY_ID = "SELECT ID, USERNAME, EMAIL, DPASSWORD FROM USERS WHERE (ID=?)";


    static {
        ResourceBundle conf = ResourceBundle.getBundle("config");
        String uri = conf.getString(CONFIG_DATABASE_URI);
        String drv = conf.getString(CONFIG_DATABASE_DRIVER);
        log.info("Creating connection pool with driver {}, uri {}", drv, uri);
        cp = ConnectionPool.builder().withDriver(drv).withUrl(uri).withLogger(LogManager.getLogger(ConnectionPool.class)).create();
    }

    @Override
    public Optional<User> getUser(String username) {
        try (Connection conn = cp.getConnection()) {
            PreparedStatement st = conn.prepareStatement(GET_USER_BY_NAME);
            st.setObject(1, username);
            log.trace(() -> "Executing query: " + GET_USER_BY_NAME + " VALUES (" + username + ")");
            ResultSet rs = st.executeQuery();
            rs.next();

            SimpleUser user = new SimpleUser();
            user.fillFromResultSet(rs);
            return Optional.of(user);

        } catch (SQLException e) {
            log.error("Error getting user id: {}", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> getUser(long id) {
        try (Connection conn = cp.getConnection()) {
            PreparedStatement st = conn.prepareStatement(GET_USER_BY_ID);
            st.setObject(1, id);
            log.trace(() -> "Executing query: " + GET_USER_BY_ID + " VALUES (" + id + ")");
            ResultSet rs = st.executeQuery();
            rs.next();
            SimpleUser user = new SimpleUser();
            user.fillFromResultSet(rs);
            return Optional.of(user);
        } catch (SQLException e) {
            log.error("Error getting user by id: {}", e);
            return Optional.empty();
        }
    }

    @Override
    public boolean authenticateUser(Optional<User> user, String password) {
        return user.map(User::getDPassword).map(dp -> dp.equals(password)).orElse(false);
    }
}
