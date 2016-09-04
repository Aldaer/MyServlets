package model.dao.databases.init;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Supplier;

class SimpleSqlConnectionSource implements Supplier<Connection> {
    private final String databaseUri;

    SimpleSqlConnectionSource(String databaseUri) {
        this.databaseUri = databaseUri;
    }

    @Override
    public Connection get() {
        try {
            return DriverManager.getConnection(databaseUri, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
