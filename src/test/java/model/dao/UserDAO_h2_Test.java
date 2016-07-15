package model.dao;

import model.dao.UserDAO;
import model.dao.UserDAO_h2;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class UserDAO_h2_Test {
    @Test
    public void testUser1() throws Exception {
        UserDAO uDao = new UserDAO_h2();
        User user = uDao.getUser("Вася").get();
        System.out.println("Вася = " + user.getId());
        assertTrue(uDao.authenticateUser(Optional.of(user), "12345"));
    }
}
