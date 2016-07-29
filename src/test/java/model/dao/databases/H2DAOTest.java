package model.dao.databases;

import model.dao.CredentialsDAO;
import model.dao.Message;
import model.dao.MessageDAO;
import model.dao.MessageDAO.MessageFilter;
import model.dao.UserDAO;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class H2DAOTest {
    private static CredentialsDAO creds;
    private static UserDAO usr;
    private static MessageDAO msg;
    private static Connection keepalive;

    @BeforeClass
    public static void createDAO() {
        try {
            Class.forName("org.h2.Driver");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        H2GlobalDAO glob = new H2GlobalDAO();

        Supplier<Connection> cs = () -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", null, null);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        };

        keepalive = cs.get();
        glob.useConnectionSource(cs);

        String[] script = {};
        try {
            script = Files.readAllLines(Paths.get("src/test/resources/InitDatabase.sql")).toArray(script);
        } catch (IOException e) {
            e.printStackTrace();
        }
        glob.executeScript(script);

        creds = glob.getCredentialsDAO();
        creds.useSaltedHash(true);
        creds.purgeTemporaryUsers(System.currentTimeMillis());
        usr = glob.getUserDAO();
        msg = glob.getMessageDAO();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        keepalive.close();
    }

    @Test
    public void getCredentialsVerifyTest() throws Exception {
        assertThat(creds.getCredentials("admin").verify("123"), is(true));
        assertThat(creds.getCredentials("admin").verify("1234"), is(false));
    }

    @Test
    public void checkIfUserExistsTest() throws Exception {
        assertThat(creds.checkIfLoginOccupied("вася"), is(true));
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
        MessageFilter.Builder bldr = MessageFilter.newBuilder().setFrom("вася");
        List<Message> messages = msg.getMessages(bldr);
        assertThat(messages.size(), is(3));
        messages.stream().forEach(System.out::println);
        System.out.println("---offset 1");
        bldr.setOffset(1L);
        messages = msg.getMessages(bldr);
        messages.stream().forEach(System.out::println);
        assertThat(messages.size(), is(2));
        System.out.println("---max 2");
        bldr.setOffset(0L);
        bldr.setLimit(2);
        messages = msg.getMessages(bldr);
        messages.stream().forEach(System.out::println);
        assertThat(messages.size(), is(2));
        System.out.println("---max 2, offset 2");
        bldr.setOffset(2L);
        messages = msg.getMessages(bldr);
        messages.stream().forEach(System.out::println);
        assertThat(messages.size(), is(1));
    }

    @Test
    public void testCountMessages() throws Exception {
        MessageFilter.Builder bld = MessageFilter.newBuilder().setFrom("вася");
        assertThat(msg.countMessages(bld), is(3));
        bld.setTo("вася");
        assertThat(msg.countMessages(bld), is(4));
        bld = MessageFilter.newBuilder();
        bld.setMinTime(Timestamp.valueOf("2015-01-01 12:05:00"));
        bld.setMaxTime(Timestamp.valueOf("2015-01-02 12:00:00"));
        assertThat(msg.countMessages(bld), is(3));
    }


    @Test
    public void testGetMessagesTimestamps() throws Exception {
        MessageFilter.Builder bld = MessageFilter.newBuilder();
        bld.setMinTime(Timestamp.valueOf("2015-01-01 12:05:00"));
        bld.setMaxTime(Timestamp.valueOf("2015-01-02 12:00:00"));
        List<Message> messages = msg.getMessages(bld);
        assertThat(messages.size(), is(3));
        messages.stream().forEach(System.out::println);
    }


    @Test
    public void testGetMessagesTextSearch() throws Exception {
        MessageFilter.Builder bld = MessageFilter.newBuilder().setTextLike("%никому%");
        List<Message> messages = msg.getMessages(bld);
        assertThat(messages.size(), is(1));
        messages.stream().forEach(System.out::println);
    }

    @Test
    public void testListUsersLike() throws Exception {
        Map<String, String> list1 = usr.listUsers("ася", 20);
        assertThat(list1.size(), is(1));
        list1.forEach((s, s2) -> System.out.println(s + " -- " + s2));
        list1 = usr.listUsers("вас", 20);
        assertThat(list1.size(), is(2));
        list1.forEach((s, s2) -> System.out.println(s + " -- " + s2));
    }
}