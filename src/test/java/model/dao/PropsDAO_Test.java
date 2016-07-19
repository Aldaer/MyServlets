package model.dao;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("All")
public class PropsDAO_Test {
    static UserDAO udao;
    static CredentialsDAO cdao;

    @BeforeClass
    public static void setUp() throws Exception {
        udao = new UserDAO_props();
        cdao = new CredsDAO_props();
        cdao.useSaltedHash(true);
    }

    @Test
    public void testGetUserByName() throws Exception {
        assertThat(udao.getUser("Вася").getId(), is(123L));
        assertThat(udao.getUser("Петя").getId(), is(456L));
        assertThat(udao.getUser("Миша").getId(), is(789L));
    }

    @Test
    public void testGetUserByID() throws Exception {
        assertThat(udao.getUser(123).getUsername(), is("вася"));
        assertThat(udao.getUser(456).getUsername(), is("петя"));
        assertThat(udao.getUser(789).getUsername(), is("миша"));
    }

    @Test
    public void testAuthenticateUser() throws Exception {
        assertThat(cdao.getCredentials("Вася").verify("qwerty"), is(true));
        assertThat(cdao.getCredentials("Петя").verify("qwerty"), is(true));
        assertThat(cdao.getCredentials("Миша").verify("qwerty"), is(true));
    }

    @Test
    public void testCaseInsensitiveUser() throws Exception {
        assertThat(cdao.getCredentials("МишА").verify("qwerty"), is(true));
    }

    @Test
    public void testWrongUsername() throws Exception {
        assertThat(cdao.getCredentials("Бася").verify("qwerty"), is(false));
    }

    @Test
    public void testWrongPassword() throws Exception {
        assertThat(cdao.getCredentials("Вася").verify("qwertz"), is(false));
    }
}