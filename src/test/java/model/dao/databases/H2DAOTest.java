package model.dao.databases;

import model.dao.CredentialsDAO;
import model.dao.Message;
import model.dao.MessageDAO;
import model.dao.MessageDAO.MessageFilter;
import model.dao.UserDAO;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class H2DAOTest {
    private static CredentialsDAO creds;
    private static UserDAO usr;
    private static MessageDAO msg;

    @BeforeClass
    public static void createDAO() {
        try {
            Class.forName("org.h2.Driver");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        H2GlobalDAO glob = new H2GlobalDAO();
        glob.useConnectionSource(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:file:E:/Programming/Java/MyServlets/src/test/userdatabase;SCHEMA=TESTDATA", "sa", "123");
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
        creds = glob.getCredentialsDAO();
        creds.useSaltedHash(true);
        creds.purgeTemporaryUsers(System.currentTimeMillis());
        usr = glob.getUserDAO();
        msg = glob.getMessageDAO();
    }


    @Test
    public void getCredentialsVerifyTest() throws Exception {
        assertThat(creds.getCredentials("admin").verify("123"), is(true));
        assertThat(creds.getCredentials("admin").verify("1234"), is(false));
    }

    @Test
    public void checkIfUserExistsTest() throws Exception {
        assertThat(creds.checkIfLoginOccupied("AdmiN"), is(true));
        assertThat(creds.checkIfLoginOccupied("AdmiNN"), is(false));
        assertThat(creds.checkIfLoginOccupied("_perm_user"), is(true));
        assertThat(creds.checkIfLoginOccupied("петя"), is(true));
    }

    @Test
    public void createTemporaryUserTest() throws Exception {
        assertThat(creds.checkIfLoginOccupied("костя_"), is(false));
        assertThat(creds.createTemporaryUser("костя_"), is(true));
        assertThat(creds.checkIfLoginOccupied("костя_"), is(true));
        assertThat(creds.createTemporaryUser("костя_"), is(false));
    }

    @Test
    public void purgeTemporaryUsersTest() throws Exception {
        assertThat(creds.checkIfLoginOccupied("толя_"), is(false));
        creds.createTemporaryUser("толя_");
        assertThat(creds.checkIfLoginOccupied("толя_"), is(true));
        Thread.sleep(100);
        creds.purgeTemporaryUsers(System.currentTimeMillis());
        assertThat(creds.checkIfLoginOccupied("толя_"), is(false));
    }

    @Test
    public void testGetMessages() throws Exception {
        System.out.println("- from вася");
        MessageFilter.Builder bld = MessageFilter.newBuilder().setFrom("вася");
        List<Message> messages = msg.getMessages(bld);
        assertThat(messages.size(), is(3));
        messages.stream().forEach(System.out::println);
        System.out.println("---skip 1");
        bld.setSkip(1);
        messages = msg.getMessages(bld);
        messages.stream().forEach(System.out::println);
        assertThat(messages.size(), is(2));
        System.out.println("---max 2");
        bld.setSkip(0);
        bld.setMaxReturned(2);
        messages = msg.getMessages(bld);
        messages.stream().forEach(System.out::println);
        assertThat(messages.size(), is(2));
        System.out.println("---max 2, skip 2");
        bld.setSkip(2);
        messages = msg.getMessages(bld);
        messages.stream().forEach(System.out::println);
        assertThat(messages.size(), is(1));

        System.out.println("-----");
        System.out.println("- to вася");
        bld = MessageFilter.newBuilder().setTo("вася");
        messages = msg.getMessages(bld);
        assertThat(messages.get(0).getFrom(), is("петя"));
        messages.stream().forEach(System.out::println);
    }
}