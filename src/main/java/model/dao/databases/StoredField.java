package model.dao.databases;

import java.lang.annotation.*;
import java.lang.reflect.Field;

/**
 * Indicates that the field is stored in the database. Allows to set the column name and optional constraint.
 * Use with objects implementing interface {@link Stored}.
 * To avoid security issues, annotating {@code final} fields with {@code StoredField} is not recommended. *
 */
@SuppressWarnings("WeakerAccess")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface StoredField {
    /**
     * Table column to store field
     * @return column name
     */
    String column() default "";

    /**
     * Shorthand for {@code column()}. Setting both will result in {@link IllegalArgumentException}.
     * @return column name
     */
    String value() default "";

    /**
     * Maximum String length, -1 for unlimited
     * @return Max length
     */
    int maxLength() default -1;

    /**
     * True for auto-generated fields (read, but ignore when stored)
     * False for normal fields
     * @return Whether the field is auto-generated
     */
    boolean auto() default false;
}

@SuppressWarnings("WeakerAccess")
/**
 * Utility class to store field data read from annotations
 */
class DbFieldData {
    final Field f;
    final String fType;
    final String columnName;
    final int maxLength;
    final boolean auto;

    DbFieldData(Field f, String columnName, int maxLength, boolean auto) {
        this.f = f;
        fType = f.getType().getCanonicalName().intern();
        this.columnName = columnName.intern();
        this.maxLength = maxLength;
        this.auto = auto;
    }

}
