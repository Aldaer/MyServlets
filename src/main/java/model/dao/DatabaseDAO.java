package model.dao;

import java.sql.Connection;
import java.util.function.Supplier;

/**
 * Common interface for global DAO classes using a relational database
 */
public interface DatabaseDAO {
    /**
     * Sets a connection source to be used by this DAO.
     * @param src Connection factory, pool etc
     */
    void useConnectionSource(Supplier<Connection> src);

    Supplier<Connection> getCurrentConnectionSource();
}
