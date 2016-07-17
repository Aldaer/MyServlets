package model.dao;

import com.aldor.utils.CryptoUtils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Supplier;

/**
 * User data access object
 */
class UserDAO_props implements UserDAO {
    private static Map<Long, String> userNames;
    private static Map<Long, String> userPwds;

    private static final String USER_TABLE = "users";
    private static final String PASSWORD_TABLE = "passwords";
    private boolean usingSaltedHash = true;


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
    public User getUser(String username) {
        return userNames.entrySet().parallelStream().filter(un -> un.getValue().equalsIgnoreCase(username)).findAny()
                .map(idName -> new SimpleUser(idName.getKey(), idName.getValue(), userPwds.get(idName.getKey()))).orElse(null);
    }

    @Override
    public User getUser(long id) {
        return userNames.containsKey(id) ? new SimpleUser(id, userNames.get(id), userPwds.get(id)) : null;
    }

    @Override
    public boolean authenticateUser(User user, String password) {
        return Optional.ofNullable(user).map(User::getDPassword)
                .map(pwd -> usingSaltedHash ? CryptoUtils.verifySaltedHash(pwd, password) : pwd.equals(password))
                .orElse(false);
    }

    @Override
    public void useSaltedHash(boolean doUse) {
        usingSaltedHash = doUse;
    }

    @Override
    public void useConnectionSource(Supplier<Connection> src) { }
}
