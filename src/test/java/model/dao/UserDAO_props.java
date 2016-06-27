package model.dao;

import model.utils.CryptoUtils;

import java.util.*;

/**
 * User data access object
 */
public class UserDAO_props implements UserDAO {
    private static Map<Long, String> userNames;
    private static Map<Long, String> userPwds;

    private static final String USER_TABLE = "users";
    private static final String PASSWORD_TABLE = "passwords";


    @SuppressWarnings("WeakerAccess")
    public UserDAO_props() {
        if (userNames == null) userNames = new HashMap<>();
        if (userPwds == null) userPwds = new HashMap<>();
        ResourceBundle userN = ResourceBundle.getBundle(USER_TABLE);
        userN.keySet().forEach(key -> userNames.put(Long.decode(key), userN.getString(key)));
        ResourceBundle userP = ResourceBundle.getBundle(PASSWORD_TABLE);
        userP.keySet().forEach(key -> userPwds.put(Long.decode(key), userP.getString(key)));
    }

    @Override
    public Optional<Long> getIdByName(String username) {
        return userNames.entrySet().parallelStream().filter(un -> un.getValue().equalsIgnoreCase(username)).findAny().map(Map.Entry::getKey);
    }

    @Override
    public User getUser(long id) {
        if (!userNames.containsKey(id)) return null;
        return new SimpleUser(id).setUsername(userNames.get(id));
    }

    @Override
    public boolean authenticateUser(long id, String password) {
        assert userPwds.containsKey(id);
        return CryptoUtils.verifySaltedHash(userPwds.get(id), password);
    }
}
