package model.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * User data access object
 */
class UserDAO_props implements UserDAO {
    private static Map<Long, String> userData = new HashMap<>();

    static {
        ResourceBundle userN = ResourceBundle.getBundle("users");
        userN.keySet().forEach(key -> userData.put(Long.decode(key), userN.getString(key)));
    }

    @Override
    public User getUser(String username) {
        return userData.keySet().parallelStream().filter(uid -> userData.get(uid).split(",", 2)[0].equalsIgnoreCase(username)).findAny()
                .map(uid -> {
                    String[] ud = userData.get(uid).split(",");
                    return new User(uid, ud[0], ud[1], ud[2], true);
                }).orElse(null);
    }

    @Override
    public User getUser(long id) {
        if (!userData.containsKey(id)) return null;
        String[] uData = userData.get(id).split(",");
        return new User(id, uData[0], uData[1], uData[2], true);
    }

    @Override
    public void updateUserInfo(User user) {
        userData.put(user.id, user.username + "," + user.fullName + "," + user.email);
    }
}
