package model.dao;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Common functionality for all DAO classes
 */
public interface DBDaoClass {

    /**
     * Returns a set of all object fields stored in the database
     * @return Set of all object fields stored in the database, never null
     */
    Set<String> getFieldNames();

    /**
     * Sets the field {@code name} to value {@value}
     * @param name Object field name
     * @param value Field value
     */
    void setField(String name, Object value);

    /**
     * Returns field value by name
     * @param name Object field name
     * @return Field value
     */
    Object getField(String name);

    /**
     * Returns class' logger, null if the class isn't logging
     * @return Class logger
     */
    @Nullable
    Logger getLogger();

    /**
     * Reconstructs DAO object from a result set
     * @param rs Result Set to recreate object from
     */
    default void fillFromResultSet(ResultSet rs) {
        getFieldNames().stream().forEach(field -> {
            try {
                setField(field, rs.getObject(field));
            } catch (SQLException e) {
                Logger log = getLogger();
                if (log != null) log.error("Error getting the field \"{}\" from result set: {}", field, e);
            }
        });
    }

    /**
     * Updates current row of the Result set with object's data
     * @param rs Result set to update
     */
    default void flushIntoResultSet(ResultSet rs) {
        getFieldNames().stream().forEach(field -> {
            try {
                rs.updateObject(field, getField(field));
            } catch (SQLException e) {
                Logger log = getLogger();
                if (log != null) log.info("Error setting the field \"{}\" in result set: {}", field, e);
            }
        });
    }

    static <T extends DBDaoClass> T createFromResultSet(Supplier<T> objSupplier, ResultSet rs) {
        T instance = objSupplier.get();
        instance.fillFromResultSet(rs);
        return instance;
    }

    static String trimToSize(String text, int length) {
        String s = text.trim();
        return (s.length() > length)? s.substring(0, length) : s;
    }
}
