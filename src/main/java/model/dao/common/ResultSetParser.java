package model.dao.common;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Generic utility class that can reconstructs objects from result set or inject them into result set.
 * Only simple type fields are supported!
 * Object fields to fill from database must be annotated with {@code @StoredField}
 */
public class ResultSetParser {
    /**
     * Reconstructs a single object from a result set. Does not affect result set cursor. Result set must have all required columns.
     * @param rs Result set to read from
     * @param objSource Object factory
     * @param <T> Object type
     * @return Reconstructed object
     * @throws SQLException
     */
    static <T extends Stored> T reconstructObject(ResultSet rs, Supplier<T> objSource) throws SQLException {
        T obj = objSource.get();
        DbFieldData[] fieldData = getClassFieldData(obj.getClass());
        int[] resultColumns = getRSColumns(rs, fieldData);
        fillFields(rs, obj, fieldData, resultColumns, false);
        return obj;
    }

    /**
     * Updates and object's fields from a result set. Does not affect result set cursor.
     * Only the fields present in the result set are affected.
     * @param rs Result set to read from
     * @param obj Object to update
     * @param <T> Object type
     * @throws SQLException
     */
    static <T extends Stored> void updateObject(ResultSet rs, T obj) throws SQLException {
        DbFieldData[] fieldData = getClassFieldData(obj.getClass());
        int[] resultColumns = getRSColumns(rs, fieldData);
        fillFields(rs, obj, fieldData, resultColumns, true);
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
    static <T extends Stored> void reconstructAllObjects(ResultSet rs, Supplier<T> objSource, Collection<T> destination) throws SQLException {
        if (rs.isClosed() || rs.isAfterLast()) return;
        boolean firstLoop = true;
        DbFieldData[] fieldData = null;
        int[] resultColumns = null;

        if (rs.isBeforeFirst()) rs.next();
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

    private static <T extends Stored> void fillFields(ResultSet rs, T obj, DbFieldData[] fieldData, int[] columns, boolean ignoreAbsent) throws SQLException {
        try {
            for (int i = 0; i < fieldData.length; i++) {
                if (!ignoreAbsent && columns[i] < 0)
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
        } catch (IllegalAccessException e) {
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

        classInfoLock.writeLock().lock();
        try {
            data = DB_CLASS_INFO_CACHE.get(cName);
            if (data != null) return data;

            Set<DbFieldData> dataSet = new HashSet<>();
            Field[] fields = c.getDeclaredFields();
            for (Field f : fields)
                if (f.isAnnotationPresent(StoredField.class)) {
                    f.setAccessible(true);
                    StoredField asf = f.getAnnotation(StoredField.class);
                    dataSet.add(new DbFieldData(f, asf.column(), asf.maxLength()));
                }
            data = dataSet.toArray(new DbFieldData[dataSet.size()]);
            DB_CLASS_INFO_CACHE.put(cName, data);
            return data;
        } finally {
            classInfoLock.writeLock().unlock();
        }
    }

    private static final Map<String, DbFieldData[]> DB_CLASS_INFO_CACHE = new HashMap<>();
    private static final ReentrantReadWriteLock classInfoLock = new ReentrantReadWriteLock();
}

@SuppressWarnings("WeakerAccess")
class DbFieldData {
    final Field f;
    final String fType;
    final String columnName;
    final int maxLength;

    DbFieldData(Field f, String columnName, int maxLength) {
        this.f = f;
        fType = f.getType().getCanonicalName().intern();
        this.columnName = columnName;
        this.maxLength = maxLength;
    }

}
