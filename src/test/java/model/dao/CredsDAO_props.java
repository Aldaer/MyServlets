package model.dao;

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
        userP.keySet().forEach(key -> userPwds.put(key.toLowerCase(), userP.getString(key)));
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
}
