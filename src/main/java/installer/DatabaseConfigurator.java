package installer;

import model.dao.databases.GenericSqlDAO;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Driver;

/**
 * Installation class for SQL databases. Creates database and necessary tables.
 */
public class DatabaseConfigurator {

    public static void main(String[] args) {
        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("src/main/java/installer/database-configure.xml");

        final GenericSqlDAO globalDao = context.getBean("globalDao", GenericSqlDAO.class);
        String scriptFile = context.getBean("initScript", String.class);
        context.getBean(Driver.class);

        String[] script = {};
        try {
            script = Files.readAllLines(Paths.get(scriptFile)).toArray(script);
        } catch (IOException e) {
            e.printStackTrace();
        }
        globalDao.executeScript(script);
    }

}
