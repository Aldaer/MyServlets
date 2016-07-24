package model.dao.common;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This interface indicates that the object is intended to be stored in the database.
 * Annotate fields to store/retrieve with {@link StoredField}. Only declared fields are analyzed, so
 * this annotation is effectively non-inherited.
 * To read an entire collection from result set more efficiently, use public {@link Processor} methods.
 * Classes implementing this interface should implement constraints indicated by {@link StoredField} annotation
 * to avoid SQL exceptions when inserting or updating objects into the database.
 */
public interface Stored {
    /**
     * Updates an object's fields from a result set. Does not affect result set cursor.
     * Only the fields present in the result set are affected.
     * @param rs  Result set to read from
     * @throws SQLException
     */
    default void updateFromResultSet(ResultSet rs) throws SQLException {
        Processor.updateObject(rs, this);
    }

    /**
     * Generates an SQL string to insert this object into a table of a database. The string will be identical
     * for any other objects of the same class.
     * @param table Table name to insert to
     * @return SQL INSERT string
     */
    default String generateInsertSQL(String table) {
        return Processor.generateInsertStatement(table, this);
    }

    /**
     * Puts field values as parameters of a prepared SQL statement.
     * Column order of the statement MUST be the same as in query returned by generateInsertSQL.
     * @param pst Statement prepared from an INSERT string returned by (@code generateInsertSQL}.
     */
    default void packIntoPreparedStatement(PreparedStatement pst) throws SQLException {
        Processor.packIntoPreparedStatement(pst, this);
    }

    /**
     * Puts field values into current position of a result set. Use with result sets produced by "SELECT ... FOR UPDATE" statements.
     * @param rs Target result set
     */
    default void injectIntoResultSet(ResultSet rs) throws SQLException {
        Processor.injectIntoResultSet(rs, this);
    }

    default String getColumnForField(String fieldName) {
        DbFieldData[] fData = Processor.getClassFieldData(this.getClass());
        return Stream.of(fData).filter(fld -> fld.f.getName().equals(fieldName)).findAny().map(fld -> fld.columnName).orElse(null);
    }

    class Processor {
        /**
         * Reconstructs a single object from a result set. Does NOT affect result set cursor,
         * so you must call {@code next()} on new ResultSet before calling this method.
         * Result set must have ALL required columns.
         *
         * @param rs        Result set to read from
         * @param objSource Object factory
         * @param <T>       Object type
         * @return Reconstructed object
         * @throws SQLException
         */
        public static <T extends Stored> T reconstructObject(ResultSet rs, Supplier<T> objSource) throws SQLException {
            T obj = objSource.get();
            DbFieldData[] fieldData = getClassFieldData(obj.getClass());
            int[] resultColumns = getRSColumns(rs, fieldData);
            fillFields(rs, obj, fieldData, resultColumns, false);
            return obj;
        }

        /**
         * Reads all objects from result set into a collection starting from current row,
         * or first row if current cursor position is before first.
         *
         * @param rs          Result set to read from
         * @param objSource   Object factory
         * @param destination Collection to put objects into
         * @param <T>         Object type
         * @throws SQLException
         */
        public static <T extends Stored> void reconstructAllObjects(ResultSet rs, Supplier<T> objSource, Collection<T> destination) throws SQLException {
            if (rs.isClosed() || rs.isAfterLast()) return;
            boolean firstLoop = true;
            DbFieldData[] fieldData = null;
            int[] resultColumns = null;

            if (rs.isBeforeFirst()) {
                if (!rs.next()) return;
            }
            do {
                T obj = objSource.get();
                if (firstLoop) {
                    fieldData = getClassFieldData(obj.getClass());
                    resultColumns = getRSColumns(rs, fieldData);
                    firstLoop = false;
                }
                fillFields(rs, obj, fieldData, resultColumns, false);
                destination.add(obj);
            } while (rs.next());
        }

        private static <T extends Stored> void updateObject(ResultSet rs, T obj) throws SQLException {
            DbFieldData[] fieldData = getClassFieldData(obj.getClass());
            int[] resultColumns = getRSColumns(rs, fieldData);
            fillFields(rs, obj, fieldData, resultColumns, true);
        }

        private static <T extends Stored> void fillFields(ResultSet rs, T obj, DbFieldData[] fieldData, int[] columns, boolean ignoreAbsent) throws SQLException {
            try {
                for (int i = 0; i < fieldData.length; i++) {
                    if (columns[i] < 0)
                        if (ignoreAbsent)
                            continue;
                        else
                            throw new RuntimeException("Column [" + fieldData[i].columnName + "] not present in result set");

                    DbFieldData fd = fieldData[i];

                    switch (fd.fType) {
                        case "byte":
                            fd.f.setByte(obj, rs.getByte(columns[i]));
                            break;
                        case "short":
                            fd.f.setShort(obj, rs.getShort(columns[i]));
                            break;
                        case "int":
                            fd.f.setInt(obj, rs.getInt(columns[i]));
                            break;
                        case "long":
                            fd.f.setLong(obj, rs.getLong(columns[i]));
                            break;
                        case "float":
                            fd.f.setFloat(obj, rs.getFloat(columns[i]));
                            break;
                        case "double":
                            fd.f.setDouble(obj, rs.getDouble(columns[i]));
                            break;
                        case "boolean":
                            fd.f.setBoolean(obj, rs.getBoolean(columns[i]));
                            break;
                        case "char":
                            fd.f.setChar(obj, rs.getString(columns[i]).charAt(0));
                            break;
                        default:
                            fd.f.set(obj, rs.getObject(columns[i]));
                    }
                }
            } catch (IllegalAccessException | IndexOutOfBoundsException e) {
                throw new RuntimeException("Error reconstructing instance of " + obj.getClass(), e);
            }
        }

        private static int[] getRSColumns(ResultSet rs, DbFieldData[] fieldData) {
            int[] result = new int[fieldData.length];

            for (int i = 0; i < fieldData.length; i++)
                try {
                    result[i] = rs.findColumn(fieldData[i].columnName);
                } catch (SQLException e) {
                    result[i] = -1;
                }
            return result;
        }

        private static DbFieldData[] getClassFieldData(Class c) {
            DbFieldData data[];
            String cName = c.getCanonicalName();

            classInfoLock.readLock().lock();
            try {
                data = DB_CLASS_INFO_CACHE.get(cName);
                if (data != null) return data;
            } finally {
                classInfoLock.readLock().unlock();
            }

            // No class data found in the cache, build it
            classInfoLock.writeLock().lock();
            try {
                // Still no data in cache?
                data = DB_CLASS_INFO_CACHE.get(cName);
                if (data != null) return data;

                Field[] fields = c.getDeclaredFields();
                data = new DbFieldData[fields.length];
                int fCount = 0;
                for (Field f : fields)
                    if (f.isAnnotationPresent(StoredField.class)) {
                        f.setAccessible(true);
                        StoredField asf = f.getAnnotation(StoredField.class);
                        if (! (asf.column().equals("") || asf.value().equals("")))
                            throw new IllegalArgumentException(
                                    "Both value() and column() attributes were set for the StoredField annotation on class " + c.getCanonicalName());
                        data[fCount++] = new DbFieldData(f, asf.column() + asf.value(), asf.maxLength(), asf.auto());
                    }
                data = Arrays.copyOfRange(data, 0, fCount);
                DB_CLASS_INFO_CACHE.put(cName, data);
                return data;
            } finally {
                classInfoLock.writeLock().unlock();
            }
        }

        private static final Map<String, DbFieldData[]> DB_CLASS_INFO_CACHE = new HashMap<>();
        private static final ReentrantReadWriteLock classInfoLock = new ReentrantReadWriteLock();

        private static String generateInsertStatement(String tableName, Stored obj) {
            StringBuilder sqlB = new StringBuilder(200);
            StringBuilder qMarks = new StringBuilder(20);
            sqlB.append("INSERT INTO ").append(tableName).append(" (");

            DbFieldData[] fieldData = getClassFieldData(obj.getClass());
            for (DbFieldData fData : fieldData)
                if (!fData.auto) {                      // Field order is determined by getClassFieldData()
                    sqlB.append(fData.columnName).append(',');
                    qMarks.append("?,");
                }
            sqlB.setLength(sqlB.length() - 1);                              // Remove trailing colon
            qMarks.setLength(qMarks.length() - 1);
            sqlB.append(") VALUES (").append(qMarks).append(");");
            return sqlB.toString();
        }

        private static void packIntoPreparedStatement(PreparedStatement pst, Stored obj) throws SQLException {
            DbFieldData[] fieldData = getClassFieldData(obj.getClass());
            int colIndex = 0;

            try {
                for (DbFieldData fData : fieldData)
                    if (!fData.auto)                                      // Field order is determined by getClassFieldData()
                        switch (fData.fType) {                             // Auto-generated fields are not inserted
                            case "byte":
                                pst.setByte(++colIndex, fData.f.getByte(obj));
                                break;
                            case "short":
                                pst.setShort(++colIndex, fData.f.getShort(obj));
                                break;
                            case "int":
                                pst.setInt(++colIndex, fData.f.getInt(obj));
                                break;
                            case "long":
                                pst.setLong(++colIndex, fData.f.getLong(obj));
                                break;
                            case "float":
                                pst.setFloat(++colIndex, fData.f.getFloat(obj));
                                break;
                            case "double":
                                pst.setDouble(++colIndex, fData.f.getDouble(obj));
                                break;
                            case "boolean":
                                pst.setBoolean(++colIndex, fData.f.getBoolean(obj));
                                break;
                            case "char":
                                pst.setString(++colIndex, "" + fData.f.getChar(obj));
                                break;
                            default:
                                pst.setObject(++colIndex, fData.f.get(obj));
                        }

            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error putting field value from object " + obj + " [" + obj.getClass() + "] into statement");
            }
        }

        private static void injectIntoResultSet(ResultSet rs, Stored obj) throws SQLException {
            DbFieldData[] fieldData = getClassFieldData(obj.getClass());
            int[] resultColumns = getRSColumns(rs, fieldData);

            try {
                for (int i = 0; i < fieldData.length; i++) {
                    final DbFieldData fData = fieldData[i];
                    if (!fData.auto)                                       // Field order is determined by getClassFieldData()
                        switch (fData.fType) {                             // Auto-generated fields are ignored
                            case "byte":
                                rs.updateByte(resultColumns[i], fData.f.getByte(obj));
                                break;
                            case "short":
                                rs.updateShort(resultColumns[i], fData.f.getShort(obj));
                                break;
                            case "int":
                                rs.updateInt(resultColumns[i], fData.f.getInt(obj));
                                break;
                            case "long":
                                rs.updateLong(resultColumns[i], fData.f.getLong(obj));
                                break;
                            case "float":
                                rs.updateFloat(resultColumns[i], fData.f.getFloat(obj));
                                break;
                            case "double":
                                rs.updateDouble(resultColumns[i], fData.f.getDouble(obj));
                                break;
                            case "boolean":
                                rs.updateBoolean(resultColumns[i], fData.f.getBoolean(obj));
                                break;
                            case "char":
                                rs.updateString(resultColumns[i], "" + fData.f.getChar(obj));
                                break;
                            default:
                                rs.updateObject(resultColumns[i], fData.f.get(obj));
                        }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error putting field value from object " + obj + " [" + obj.getClass() + "] into result set");
            }
        }
    }

}
