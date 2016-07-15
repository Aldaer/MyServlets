package model.dao;

import model.dao.UserDAO;
import model.dao.UserDAO_h2;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserDAO_h2_Test {
    @Test
    public void testUserReadFromDatabase() throws Exception {
        UserDAO uDao = new UserDAO_h2();
        User user = uDao.getUser("Вася").get();
        System.out.printf("%s: id = %d, email = %s", user.getUsername(), user.getId(), user.getEmail());
        assertTrue(uDao.authenticateUser(Optional.of(user), "12345"));
        assertFalse(uDao.authenticateUser(Optional.of(user), "123456"));

    }
}
