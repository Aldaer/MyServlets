package model.dao;

import com.aldor.utils.CryptoUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static java.util.Optional.ofNullable;

@SuppressWarnings("WeakerAccess")
public class CredsDAO_props implements CredentialsDAO {
    private static Map<String, String> userPwds = new HashMap<>();
    private boolean usingSaltedHash;

    static {
        ResourceBundle userP = ResourceBundle.getBundle("passwords");
        userP.keySet().forEach(un -> userPwds.put(un.toLowerCase(), userP.getString(un)));
    }

    @Override
    public Credentials getCredentials(String username) {
        String lcName = username.toLowerCase();
        return ofNullable(userPwds.get(lcName)).map(pwd -> new Credentials(lcName, pwd, usingSaltedHash)).orElse(null);
    }

    @Override
    public void useSaltedHash(boolean doUse) {
        usingSaltedHash = doUse;
    }

    @Override
    public boolean checkIfUserExists(String username) {
        return (userPwds.containsKey(username.toLowerCase()));
    }

    @Override
    public boolean createTemporaryUser(String username) {
        if (checkIfUserExists(username)) return false;
        userPwds.put(username.toLowerCase(), "" + System.currentTimeMillis());
        return true;
    }

    @Override
    public void purgeTemporaryUsers(long timeThreshold) {
        userPwds.keySet().parallelStream().filter(un -> {
            try {
                return Long.valueOf(userPwds.get(un)) < timeThreshold;
            } catch (NumberFormatException e) {
                return false;
            }
        }).forEach(userPwds::remove);
    }

    @Override
    public Credentials storeNewCredentials(String username, String password) {
        if (usingSaltedHash)
            password = CryptoUtils.stringRandomSaltedHash(password);
        userPwds.put(username, password);
        return new Credentials(username, password, usingSaltedHash);
    }
}
