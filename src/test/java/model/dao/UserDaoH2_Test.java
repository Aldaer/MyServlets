package model.dao;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserDaoH2_Test {
    @Test
    public void testUserReadFromDatabase() throws Exception {
        UserDAO uDao = new UserDaoH2();
        User user = uDao.getUser("Вася");
        System.out.printf("%s: id = %d, email = %s", user.getUsername(), user.getId(), user.getEmail());
        assertTrue(uDao.authenticateUser(user, "12345"));
        assertFalse(uDao.authenticateUser(user, "1234"));

    }
}
