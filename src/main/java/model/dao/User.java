package model.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.dao.databases.Stored;
import model.dao.databases.StoredField;

/**
 * User details
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User implements Stored {
    @StoredField(auto = true, column = "id")
    long id = 0;

    @StoredField(column = "username", maxLength = 50)
    String username = "";

    @Setter
    @StoredField("fullname")
    String fullName = "";

    @Setter
    @StoredField(column = "email", maxLength = 100)
    String email = "";

    @Setter
    @StoredField("regcomplete")
    boolean regComplete = false;

    public User(String username, String fullName, String email, boolean regComplete) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.regComplete = regComplete;
    }

}


