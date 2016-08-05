package model.dao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.dao.databases.Stored;
import model.dao.databases.StoredField;

import java.sql.Timestamp;

/**
 * Conversation is started by a user and can be joined by other users. It contains messages and message chains.
 */
@Getter
@NoArgsConstructor
public class Conversation implements Stored {
    @Setter
    @StoredField(auto = true, column = "id")
    long id;

    @StoredField(column = "name", maxLength = 100)
    String name="";

    @StoredField(column = "description", maxLength = 255)
    String desc="";

    @StoredField(column="starter", maxLength = 50)
    String starter="";

    @StoredField(auto = true, column = "started")
    Timestamp started;

    public Conversation(String name, String decription, String starterName) {
        this.name = name;
        desc = decription;
        starter = starterName;
    }
}
