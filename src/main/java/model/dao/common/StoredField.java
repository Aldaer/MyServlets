package model.dao.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the field is stored in the database. Allows to set the column name and optional constraint.
 */
@SuppressWarnings("WeakerAccess")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StoredField {
    String column() default "";

    int maxLength() default -1;
}

/**
 * Flag interface, indicates that object is intended to be stored in the database.
 * Annotate fields to store/retrieve with {@link StoredField}
 */
interface Stored {}
