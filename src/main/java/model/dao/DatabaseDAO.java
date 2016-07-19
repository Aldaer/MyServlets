package model.dao;

import java.sql.Connection;
import java.util.function.Supplier;

/**
 * Common interface for all DAO classes using a relational database
 */
public interface DatabaseDAO {
    /**
     * Sets a connection source to be used by this DAO. May be different from the one Global DAO is using
     * @param src Connection factory, pool etc
     */
    void useConnectionSource(Supplier<Connection> src);
}
