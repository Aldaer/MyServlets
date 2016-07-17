package model.dao;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interface for generic User class
 */
public interface User {
    Long getId();
    String getUsername();
    String getEmail();
    String getDPassword();

    enum UserDatabaseFields { ID(0), USERNAME(50), FULLNAME(255), EMAIL(100), DPASSWORD(40);
        final int lengthConstraint;
        UserDatabaseFields(int lengthConstraint) {
            this.lengthConstraint = lengthConstraint;
        }
    }
    Set<String> USER_DATABASE_FIELDS = Stream.of(UserDatabaseFields.values()).map(Enum::name).collect(Collectors.toSet());
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

    SimpleUser(long id, String username, String dPassword) {
        this.id = id;
        this.username = username;
        this.dPassword = dPassword;
    }

    SimpleUser() {}

    @Override
    public Set<String> getFieldNames() {
        return USER_DATABASE_FIELDS;
    }


    @Override
    public void setField(String name, Object value) {
        switch (UserDatabaseFields.valueOf(name)) {
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
        switch (UserDatabaseFields.valueOf(name)) {
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


