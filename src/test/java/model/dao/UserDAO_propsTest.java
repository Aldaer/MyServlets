package model.dao;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class UserDAO_propsTest {
    UserDAO udao = new UserDAO_props();

    @Test
    public void testGetIdByName() throws Exception {
        assertThat(udao.getIdByName("Вася"), is(Optional.of(123L)));
        assertThat(udao.getIdByName("Петя"), is(Optional.of(456L)));
        assertThat(udao.getIdByName("Миша"), is(Optional.of(789L)));
    }

    @Test
    public void testGetUser() throws Exception {
        assertThat(udao.getUser(123).getUserName(), is("Вася"));
        assertThat(udao.getUser(456).getUserName(), is("Петя"));
        assertThat(udao.getUser(789).getUserName(), is("Миша"));
    }

    @Test
    public void testAuthenticateUser() throws Exception {
        assertThat(udao.authenticateUser("Вася", "qwerty"), is(true));
        assertThat(udao.authenticateUser("Петя", "qwerty"), is(true));
        assertThat(udao.authenticateUser("Миша", "qwerty"), is(true));
    }

    @Test
    public void testCaseInsensitiveUser() throws Exception {
        assertThat(udao.authenticateUser("вася", "qwerty"), is(true));
    }

    @Test
    public void testWrongUsername() throws Exception {
        assertThat(udao.authenticateUser("Васю", "qwerty"), is(false));
    }

    @Test
    public void testWrongPassword() throws Exception {
        assertThat(udao.authenticateUser("Вася", "Qwerty"), is(false));
    }
}