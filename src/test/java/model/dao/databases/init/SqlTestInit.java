package model.dao.databases.init;

import model.dao.ConversationDAO;
import model.dao.CredentialsDAO;
import model.dao.MessageDAO;
import model.dao.UserDAO;
import model.dao.databases.GenericSqlDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.function.Supplier;

// Actual context to inject beans from is selected by profile activated with annotation in test class
@Configuration
public class SqlTestInit {
    @Resource
    private String initScriptPath;

    private CredentialsDAO creds;

    private UserDAO usr;

    private MessageDAO msg;

    private ConversationDAO convs;

    @Resource
    private GenericSqlDAO globalDao;

    @Resource(name="secondaryConnectionSource")
    private Supplier<Connection> testRunConnectionSource;

    @PostConstruct
    private void runInitScript() {
        String[] script = {};
        try {
            script = Files.readAllLines(Paths.get(initScriptPath)).toArray(script);
        } catch (IOException e) {
            e.printStackTrace();
        }
        globalDao.executeScript(script);

        globalDao.useConnectionSource(testRunConnectionSource);

        creds = globalDao.getCredentialsDAO();
        creds.useSaltedHash(true);
        creds.purgeTemporaryUsers(System.currentTimeMillis());
        usr = globalDao.getUserDAO();
        msg = globalDao.getMessageDAO();
        convs = globalDao.getConversationDAO();
    }

    @Bean
    public CredentialsDAO getCreds() {
        return this.creds;
    }

    @Bean
    public UserDAO getUsr() {
        return this.usr;
    }

    @Bean
    public MessageDAO getMsg() {
        return this.msg;
    }

    @Bean
    public ConversationDAO getConvs() {
        return this.convs;
    }
}
