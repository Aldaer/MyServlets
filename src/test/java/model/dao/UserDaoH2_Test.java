package model.dao;

import dbconnecton.ConnectionPool;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserDaoH2_Test {
    private static ConnectionPool connectionPool;

    @BeforeClass
    public static void setUp() throws Exception {
        String uri = "jdbc:h2:file:E:/Programming/Java/MyServlet/src/test/resources;SCHEMA=USERDATA";
        String drv = "org.h2.Driver";
        String un = "sa";
        String pwd = "123";
        connectionPool = ConnectionPool.builder()
                .withDriver(drv)
                .withUrl(uri)
                .withUserName(un)
                .withPassword(pwd)
                .withLogger(LoggerFactory.getLogger(ConnectionPool.class))
                .create();
    }

    @Test
    public void testUserReadFromDatabase() throws Exception {
        UserDAO uDao = new UserDaoH2();
        uDao.useConnectionSource(connectionPool);
        uDao.useSaltedHash(true);
        User user = uDao.getUser("Вася");
        System.out.printf("%s: id = %d, email = %s", user.getUsername(), user.getId(), user.getEmail());
        assertTrue(uDao.authenticateUser(user, "12345"));
        assertFalse(uDao.authenticateUser(user, "1234"));

    }
}
