package model.dao.databases.init;

import lombok.Getter;
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

@Configuration
public class SqlTestInit {
    @Resource
    private String initScriptPath;

    @Getter(onMethod = @__({@Bean}))
    private CredentialsDAO creds;

    @Getter(onMethod = @__({@Bean}))
    private UserDAO usr;

    @Getter(onMethod = @__({@Bean}))
    private MessageDAO msg;

    @Getter(onMethod = @__({@Bean}))
    private ConversationDAO convs;

    @Resource
    private GenericSqlDAO globalDao;

    @Resource
    private Supplier<Connection> testSetupConnectionSource;

    @Resource
    private Supplier<Connection> testRunConnectionSource;

    @PostConstruct
    private void runInitScript() {
        globalDao.useConnectionSource(testSetupConnectionSource);

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
}
