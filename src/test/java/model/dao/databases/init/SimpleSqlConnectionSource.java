package model.dao.databases.init;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Supplier;

class SimpleSqlConnectionSource implements Supplier<Connection> {
    private final String databaseUri;
    private final String username;
    private final String password;

    SimpleSqlConnectionSource(String databaseUri, String username, String password) {
        this.databaseUri = databaseUri;
        this.username = username;
        this.password = password;
    }

    SimpleSqlConnectionSource(String databaseUri) {
        this(databaseUri, null, null);
    }

    @Override
    public Connection get() {
        try {
            return DriverManager.getConnection(databaseUri, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
