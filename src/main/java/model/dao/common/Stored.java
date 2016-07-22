package model.dao.common;

import java.sql.PreparedStatement;
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

    default String generateInsertSQL(String table) {
        return ResultSetParser.generateInsertStatement(table, this);
    }

    /**
     * Puts field values as parameters of a prepared SQL statement.
     * Only call on prepared statements made from generated SQL strings! Column order isn't checked.
     * @param pst Statement prepared from string returned by {@code generateInsertSQL} etc.
     */
    default void packIntoPreparedStatement(PreparedStatement pst) {
        ResultSetParser.packIntoPreparedStatement(pst, this);
    }
}
