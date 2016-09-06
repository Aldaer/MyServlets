package model.dao.databases;

import model.dao.*;
import model.dao.MessageDAO.MessageFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("ConstantConditions")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:MySql-test-context.xml")
@ActiveProfiles("mySql")
public class MySqlDAOTest {
    @Resource
    private CredentialsDAO creds;

    @Resource
    private UserDAO usr;

    @Resource
    private MessageDAO msg;

    @Resource
    private ConversationDAO convs;

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
        messages.forEach(System.out::println);
        System.out.println("---offset 1");
        bldr.setOffset(1L);
        messages = msg.getMessages(bldr);
        messages.forEach(System.out::println);
        assertThat(messages.size(), is(2));
        System.out.println("---max 2");
        bldr.setOffset(0L);
        bldr.setLimit(2);
        messages = msg.getMessages(bldr);
        messages.forEach(System.out::println);
        assertThat(messages.size(), is(2));
        System.out.println("---max 2, offset 2");
        bldr.setOffset(2L);
        messages = msg.getMessages(bldr);
        messages.forEach(System.out::println);
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
        messages.forEach(System.out::println);
    }


    @Test
    public void testGetMessagesTextSearch() throws Exception {
        MessageFilter.Builder bld = MessageFilter.newBuilder().setTextLike("%никому%");
        List<Message> messages = msg.getMessages(bld);
        assertThat(messages.size(), is(1));
        messages.forEach(System.out::println);
    }

    @Test
    public void testListUsersLike() throws Exception {
        Collection<ShortUserInfo> list1 = usr.listUsers("ася", 20);
        assertThat(list1.size(), is(1));
        list1.forEach(u -> System.out.println(u.getUsername() + " -- " + u.getFullName()));
        list1 = usr.listUsers("вас", 20);
        assertThat(list1.size(), is(2));
        list1.forEach(u -> System.out.println(u.getUsername() + " -- " + u.getFullName()));
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

    @Test
    public void testGetConversationByParticipant() throws Exception {
        User user = usr.getUser("петя");
        Collection<Conversation> userConvs = convs.listConversations(user.getId());
        assertThat(userConvs.size(), is(2));
    }

    @Test
    public void testCreateConversation() throws Exception {
        User user = usr.getUser("вася");
        Collection<Conversation> convs1 = convs.listConversations(user.getId());
        Conversation newConv = convs.createConversation("тема", "описание", user);
        assertThat(newConv.getName(), is("тема"));
        Collection<Conversation> convs2 = convs.listConversations(user.getId());
        assertThat(convs2.size(), is(convs1.size() + 1));
    }

    @Test
    public void testGetUserByConversation() throws Exception {
        Collection<ShortUserInfo> convPs = usr.listParticipants(1);
        assertThat(convPs.size(), is(2));
    }

    @Test
    public void testInviteAndJoin() throws Exception {
        User user = usr.getUser("вася");
        Conversation newConv = convs.createConversation("topic1", "desc1", user);
        long u2id = usr.getUser("петя").getId();
        int numConvs = convs.listConversations(u2id).size();
        Collection<Conversation> u2invites = convs.listInvites(u2id);
        assertThat(u2invites.size(), is(0));
        convs.inviteToConversation(newConv.getId(), u2id);
        u2invites = convs.listInvites(u2id);
        assertThat(u2invites.size(), is(1));
        assertThat(u2invites.iterator().next().getName(), is("topic1"));
        convs.joinConversation(newConv.getId(), u2id);
        u2invites = convs.listInvites(u2id);
        assertThat(u2invites.size(), is(0));
        int newNumConvs = convs.listConversations(u2id).size();
        assertThat(newNumConvs, is(numConvs + 1));
    }

    @Test
    public void testInviteAndDecline() throws Exception {
        User user = usr.getUser("вася");
        Conversation newConv = convs.createConversation("topic2", "desc2", user);
        long u2id = usr.getUser("петя").getId();
        int numConvs = convs.listConversations(u2id).size();
        int numInvites = convs.countInvitations(u2id);
        assertThat(numInvites, is(0));
        convs.inviteToConversation(newConv.getId(), u2id);
        numInvites = convs.countInvitations(u2id);
        assertThat(numInvites, is(1));

        long[] declineList = {newConv.getId()};
        Collection<Conversation> remainingInvites = convs.acceptOrDeclineInvites(u2id, false, declineList);
        assertThat(remainingInvites.size(), is(0));
        numInvites = convs.countInvitations(u2id);
        assertThat(numInvites, is(0));
        int newNumConvs = convs.listConversations(u2id).size();
        assertThat(newNumConvs, is(numConvs));
    }

    @Test
    public void testNoInvitesIfAlreadyIn() throws Exception {
        User user = usr.getUser("вася");
        long uid = user.getId();
        Conversation conv = convs.getConversation(1);
        assertThat(conv.getStarter(), is("вася"));
        int numInvites = convs.listInvites(uid).size();
        convs.inviteToConversation(1, uid);
        int numInvites2 = convs.listInvites(uid).size();
        assertThat(numInvites2, is(numInvites));
    }

}