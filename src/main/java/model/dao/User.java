package model.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dao.common.Stored;
import model.dao.common.StoredField;

/**
 * User details
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User implements Stored {
    @StoredField(auto = true, column = "id")
    long id;

    @StoredField(column = "username", maxLength = 50)
    String username;

    @StoredField(column = "fullname", maxLength = 255)
    String fullName;

    @StoredField(column = "email", maxLength = 100)
    String email;

    @StoredField(column = "regcomplete")
    boolean regComplete;

}


