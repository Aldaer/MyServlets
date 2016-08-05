package model.dao.databases;

import model.dao.*;
import model.dao.MessageDAO.MessageFilter;
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
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("ConstantConditions")
public class MySqlDAOTest {
    private static final String TEST_DB_NAME = "test"; // Must be the same as DB name in InitDatabase_MySql.sql

    private static CredentialsDAO creds;
    private static UserDAO usr;
    private static MessageDAO msg;
    private static ConversationDAO convs;

    private static Connection keepalive;

    @BeforeClass
    public static void createDAO() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        GenericSqlDAO glob = new MySqlGlobalDao();

        Supplier<Connection> csSetup = () -> {
            try {
                return DriverManager.getConnection("jdbc:mysql://localhost:3306?useSSL=false", "epam", "1234");
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        };

        keepalive = csSetup.get();
        glob.useConnectionSource(csSetup);

        String[] script = {};
        try {
            script = Files.readAllLines(Paths.get("src/test/resources/InitDatabase_MySql.sql")).toArray(script);
        } catch (IOException e) {
            e.printStackTrace();
        }
        glob.executeScript(script);

        Supplier<Connection> csRun = () -> {
            try {
                return DriverManager.getConnection("jdbc:mysql://localhost:3306/" + TEST_DB_NAME + "?useSSL=false", "epam", "1234");
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        };
        glob.useConnectionSource(csRun);

        creds = glob.getCredentialsDAO();
        creds.useSaltedHash(true);
        creds.purgeTemporaryUsers(System.currentTimeMillis());
        usr = glob.getUserDAO();
        msg = glob.getMessageDAO();
        convs = glob.getConversationDAO();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        keepalive.createStatement().executeUpdate("DROP DATABASE " + TEST_DB_NAME + ";");
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
        messages.stream().forEach(System.out::println);
        assertThat(messages.size(), is(3));
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
        Collection<ShortUserInfo> list1 = usr.listUsers("ася", 20);
        list1.forEach(u -> System.out.println(u.getUsername() + " -- " + u.getFullName()));
        assertThat(list1.size(), is(1));
        list1 = usr.listUsers("вас", 20);
        list1.forEach(u -> System.out.println(u.getUsername() + " -- " + u.getFullName()));
        assertThat(list1.size(), is(2));
    }

    @Test
    public void testExistingFriends() throws Exception {
        long[] flist1 = usr.getFriendIds(usr.getUser("Вася").getId());
        assertThat(usr.getUser(flist1[0]).getUsername().toLowerCase(), is("петя"));
    }

    @Test
    public void testAddRemoveFriends() throws Exception {
        long id1 = usr.getUser("вася").getId();
        long[] flist1 = usr.getFriendIds(id1);
        long id2 = usr.getUser("admin").getId();
        usr.addFriend(id1, id2);
        long[] flist2 = usr.getFriendIds(id1);
        assertThat(flist2.length - flist1.length, is(1));
        usr.addFriend(id1, id2);                // Test idempotency
        long[] flist2a = usr.getFriendIds(id1);
        assertThat(flist2a.length - flist1.length, is(1));
        usr.removeFriend(id1, id2);
        long[] flist3 = usr.getFriendIds(id1);
        assertThat(flist3.length, is(flist1.length));
    }

    @Test
    public void testGetConversation() throws Exception {
        Conversation conv = convs.getConversation(1);
        assertThat(conv.getName(), is("Сообщество"));
    }
}
