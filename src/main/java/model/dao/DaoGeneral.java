package model.dao;

import java.sql.Connection;
import java.util.function.Supplier;

/**
 * Constants used by DAO objects
 */
public interface DaoGeneral {
    String CONFIG_DATABASE_URI = "database_uri";
    String CONFIG_DATABASE_DRIVER = "database_driver";
    String CONFIG_DATABASE_USER = "username";
    String CONFIG_DATABASE_PASSWORD = "password";
    String CONFIG_DATABASE_USE_SHA_DIGEST = "sha_digest";

    /**
     * Sets a connection source to be used by this DAO
     * @param src Connection factory, pool etc.
     */
    void useConnectionSource(Supplier<Connection> src);
}
