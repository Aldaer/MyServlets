package model.dao;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static model.dao.StandardDAO.USER_DATABASE_FIELDS;

/**
 * Interface for generic User class
 */
public interface User {
    Long getId();
    String getUsername();
    String getEmail();
    String getDPassword();
}


/**
 * Simple implementation of the User class. Doesn't support logging
 */
@Getter
class SimpleUser implements User, DBDaoClass {

    private String username;
    private String email;
    private String dPassword;
    private Long id;

    public SimpleUser(long id, String username, String dPassword) {
        this.id = id;
        this.username = username;
        this.dPassword = dPassword;
    }

    public SimpleUser() {}

    @Override
    public Set<String> getFieldNames() {
        return USER_DATABASE_FIELDS;
    }


    @Override
    public void setField(String name, Object value) {
        switch (StandardDAO.UserDatabaseFields.valueOf(name)) {
            case ID:
                id = (Long) value;
                break;
            case USERNAME:
                username = (String) value;
                break;
            case EMAIL:
                email = (String) value;
                break;
            case DPASSWORD:
                dPassword = (String)value;
                break;
        }
    }

    @Override
    public Object getField(String name) {
        switch (StandardDAO.UserDatabaseFields.valueOf(name)) {
            case ID:
                return getId();
            case USERNAME:
                return getUsername();
            case EMAIL:
                return getEmail();
            case DPASSWORD:
                return getDPassword();
            default:
                return null;
        }
    }

    @Override
    public @Nullable Logger getLogger() {
        return null;
    }
}
