package model.dao.common;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This interface indicates that the object is intended to be stored in the database.
 * Annotate fields to store/retrieve with {@link StoredField}
 * To read an entire collection from result set more efficiently, use {@link ResultSetParser} methods
 * (empty constructors required for those methods that take {@code Suppliers} as arguments).
 * Classes implementing this interface should implement constraints indicated by {@link StoredField} annotation
 * to avoid SQL exceptions when inserting or updating objects into the database.
 */
public interface Stored {
    default void updateFromResultSet(ResultSet rs) throws SQLException {
        ResultSetParser.updateObject(rs, this);
    }
}
