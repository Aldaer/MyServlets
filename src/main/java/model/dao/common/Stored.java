package model.dao.common;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This interface indicates that the object is intended to be stored in the database.
 * Annotate fields to store/retrieve with {@link StoredField}
 * To read an entire collection from result set more efficiently, use {@link ResultSetProcessor} methods.
 * Classes implementing this interface should implement constraints indicated by {@link StoredField} annotation
 * to avoid SQL exceptions when inserting or updating objects into the database.
 */
public interface Stored {
    default void updateFromResultSet(ResultSet rs) throws SQLException {
        ResultSetProcessor.updateObject(rs, this);
    }

    default String generateInsertSQL(String table) {
        return ResultSetProcessor.generateInsertStatement(table, this);
    }

    /**
     * Puts field values as parameters of a prepared SQL statement.
     * Column order of the statement MUST be the same as in query returned by generateInsertSQL.
     * @param pst Statement prepared from string returned by {@code generateInsertSQL} etc.
     */
    default void packIntoPreparedStatement(PreparedStatement pst) throws SQLException {
        ResultSetProcessor.packIntoPreparedStatement(pst, this);
    }

    /**
     * Puts field values into current position of a result set. Use with result sets produced by "SELECT ... FOR UPDATE" statements.
     * @param rs Target result set
     */
    default void injectIntoResultSet(ResultSet rs) throws SQLException {
        ResultSetProcessor.injectIntoResultSet(rs, this);
    }
}
