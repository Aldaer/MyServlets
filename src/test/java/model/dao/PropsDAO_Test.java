package model.dao;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
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
        assertNull(cdao.getCredentials("Бася"));
    }

    @Test
    public void testWrongPassword() throws Exception {
        assertThat(cdao.getCredentials("Вася").verify("qwertz"), is(false));
    }

    @Test
    public void testCreateNewCreds() throws Exception {
        cdao.storeNewCredentials("света", "123321");
        Credentials creds = cdao.getCredentials("света");
        assertThat(creds.verify("123321"), is(true));
        assertThat(creds.verify("123322"), is(false));
    }

    @Test
    public void testExistingFriends() throws Exception {
        long[] flist1 = udao.getFriendIds(udao.getUser("Вася").getId());
        assertThat(udao.getUser(flist1[0]).getUsername().toLowerCase(), is("петя"));
    }

    @Test
    public void testAddRemoveFriends() throws Exception {
        long id1 = udao.getUser("петя").getId();
        long[] flist1 = udao.getFriendIds(id1);
        long id2 = udao.getUser("миша").getId();
        udao.addFriend(id1, id2);
        long[] flist2 = udao.getFriendIds(id1);
        assertThat(flist2.length - flist1.length, is(1));
        udao.removeFriend(id1, id2);
        long[] flist3 = udao.getFriendIds(id1);
        assertThat(flist3.length, is(flist1.length));
    }
}