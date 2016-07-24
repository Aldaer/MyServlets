package model.dao.databases;

import model.dao.CredentialsDAO;
import model.dao.UserDAO;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class H2DAOTest {
    private static CredentialsDAO creds;
    private static UserDAO usr;

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
                return DriverManager.getConnection("jdbc:h2:file:E:/Programming/Java/MyServlets/src/test/userdatabase;SCHEMA=USERDATA", "sa", "123");
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
        creds = glob.getCredentialsDAO();
        creds.useSaltedHash(true);
        creds.purgeTemporaryUsers(System.currentTimeMillis());
        usr = glob.getUserDAO();
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
        assertThat(creds.checkIfLoginOccupied("костя"), is(false));
        assertThat(creds.createTemporaryUser("костя"), is(true));
        assertThat(creds.checkIfLoginOccupied("костя"), is(true));
        assertThat(creds.createTemporaryUser("костя"), is(false));
    }

    @Test
    public void purgeTemporaryUsersTest() throws Exception {
        assertThat(creds.checkIfLoginOccupied("толя"), is(false));
        creds.createTemporaryUser("толя");
        assertThat(creds.checkIfLoginOccupied("толя"), is(true));
        Thread.sleep(200);
        creds.purgeTemporaryUsers(System.currentTimeMillis());
        assertThat(creds.checkIfLoginOccupied("толя"), is(false));
    }

}