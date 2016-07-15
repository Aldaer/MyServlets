package model.dao;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Constants used by DAO objects
 */
public interface StandardDAO {
    enum UserDatabaseFields { ID(0), USERNAME(50), FULLNAME(255), EMAIL(100), DPASSWORD(40);
        final int lengthConstraint;
        UserDatabaseFields(int lengthConstraint) {
            this.lengthConstraint = lengthConstraint;
        }
    }
    Set<String> USER_DATABASE_FIELDS = Stream.of(UserDatabaseFields.values()).map(Enum::name).collect(Collectors.toSet());

    String CONFIG_DATABASE_URI = "database_uri";
    String CONFIG_DATABASE_DRIVER = "database_driver";
    String CONFIG_DATABASE_USER = "username";
    String CONFIG_DATABASE_PASSWORD = "password";
}
