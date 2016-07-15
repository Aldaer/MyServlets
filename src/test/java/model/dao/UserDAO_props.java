package model.dao;

import model.utils.CryptoUtils;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExistsHelper;

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
    public Optional<User> getUser(String username) {
        return userNames.entrySet().parallelStream().filter(un -> un.getValue().equalsIgnoreCase(username)).findAny()
                .map(idName -> new SimpleUser(idName.getKey(), idName.getValue(), userPwds.get(idName.getKey())));
    }

    @Override
    public Optional<User> getUser(long id) {
        return Optional.ofNullable(userNames.get(id)).map(name -> new SimpleUser(id, name, userPwds.get(id)));
    }

    @Override
    public boolean authenticateUser(Optional<User> user, String password) {
        return user.map(User::getDPassword).map(pwd -> CryptoUtils.verifySaltedHash(pwd, password)).orElse(false);
    }


}
