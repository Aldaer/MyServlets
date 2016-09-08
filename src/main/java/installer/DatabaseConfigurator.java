package installer;

import model.dao.DatabaseDAO;
import model.dao.GlobalDAO;
import model.dao.databases.GenericSqlDAO;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import static controller.ServletInitListener.*;

/**
 * Installation class for SQL databases. Creates database and necessary tables.
 * Uses project's {@code config.properties}. Database name is the last non-parameter part of database uri.
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
