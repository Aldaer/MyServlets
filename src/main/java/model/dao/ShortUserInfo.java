package model.dao;

import lombok.Getter;

/**
 * Shortened version to make JSONs of
 */
@Getter
public class ShortUserInfo {
    private final long id;
    private final String username;
    private final String fullName;

    ShortUserInfo(long id, String username, String fullName) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
    }
}
