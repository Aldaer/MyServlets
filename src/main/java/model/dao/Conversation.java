package model.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.dao.databases.Stored;
import model.dao.databases.StoredField;

import java.sql.Timestamp;

/**
 * Conversation is started by a user and can be joined by other users. It contains messages and message chains.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Conversation implements Stored {
    @StoredField(auto = true, column = "id")
    long id = 0;

    @StoredField(column = "name", maxLength = 100)
    String name="CONVERSATION";

    @StoredField(column = "description", maxLength = 255)
    String desc="";

    @StoredField(column="starter", maxLength = 50)
    String starter;

    @StoredField(auto = true, column = "started")
    Timestamp started;
}
