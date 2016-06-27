package model.dao;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Interface for generic User class
 */
public interface User extends Serializable {
    String getEmail();
    User setEmail(String email);
    String getUserName();
    User setUsername(String username);
    long getId();
}

/**
 * Simple implementation of the User class
 */
class SimpleUser implements User {
    private String username;
    private String email;
    private final long id;

    @Override
    public String getEmail() {
        synchronized (this) { return email; }
    }

    @Override
    public SimpleUser setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    public SimpleUser(@Nullable String username, @Nullable String email, long id) {
        this.username = username;
        this.email = email;
        this.id = id;
    }

    public SimpleUser(long id) {
        this.id = id;
    }

    @Override
    public String getUserName() {
        synchronized (this) { return username; }
    }

    @Override
    public SimpleUser setUsername(String username) {
        this.username = username;
        return this;
    }
}
