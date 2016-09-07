package installer;

import model.dao.DatabaseDAO;
import model.dao.GlobalDAO;
import model.dao.databases.GenericSqlDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
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
        ResourceBundle conf = ResourceBundle.getBundle(CONFIG_BUNDLE);

        GlobalDAO globalDao;
        try {
            String userDaoClass = conf.getString(CONFIG_DAO_CLASS);
            globalDao = (GlobalDAO) Class.forName(userDaoClass).newInstance();
        } catch (MissingResourceException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Global DAO config error", e);
        }

        String uri = conf.getString("database_init_uri");
        String scriptFile = conf.getString("database_init_script");
        String username = conf.getString(CONFIG_DATABASE_USER);
        String password = conf.getString(CONFIG_DATABASE_PASSWORD);

        Supplier<Connection> cs = () -> {
            try {
                return DriverManager.getConnection(uri, username, password);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        };

        ((DatabaseDAO) globalDao).useConnectionSource(cs);


        String[] script = {};
        try {
            script = Files.readAllLines(Paths.get(scriptFile)).toArray(script);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((GenericSqlDAO) globalDao).executeScript(script);
    }

}
