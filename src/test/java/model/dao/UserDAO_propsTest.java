package model.dao;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("All")
public class UserDAO_propsTest {
    UserDAO udao = new UserDAO_props();
    {
        udao.useSaltedHash(true);
    }

    @Test
    public void testGetUserByName() throws Exception {
        assertThat(udao.getUser("Вася").getId(), is(123L));
        assertThat(udao.getUser("Петя").getId(), is(456L));
        assertThat(udao.getUser("Миша").getId(), is(789L));
    }

    @Test
    public void testGetUserNyID() throws Exception {
        assertThat(udao.getUser(123).getUsername(), is("Вася"));
        assertThat(udao.getUser(456).getUsername(), is("Петя"));
        assertThat(udao.getUser(789).getUsername(), is("Миша"));
    }

    @Test
    public void testAuthenticateUser() throws Exception {
        assertThat(udao.authenticateUser(udao.getUser("Вася"), "qwerty"), is(true));
        assertThat(udao.authenticateUser(udao.getUser("Петя"), "qwerty"), is(true));
        assertThat(udao.authenticateUser(udao.getUser("Миша"), "qwerty"), is(true));
    }

    @Test
    public void testCaseInsensitiveUser() throws Exception {
        assertThat(udao.authenticateUser(udao.getUser("вася"), "qwerty"), is(true));
    }

    @Test
    public void testWrongUsername() throws Exception {
        assertThat(udao.authenticateUser(udao.getUser("васq"), "qwerty"), is(false));
    }

    @Test
    public void testWrongPassword() throws Exception {
        assertThat(udao.authenticateUser(udao.getUser("Вася"), "qwertz"), is(false));
    }
}