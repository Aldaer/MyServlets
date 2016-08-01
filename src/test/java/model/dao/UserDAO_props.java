package model.dao;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * User data access object
 */
class UserDAO_props implements UserDAO {
    private static Map<Long, String> userData = new HashMap<>();
    private static Map<Long, long[]> userFriends = new HashMap<>();

    static {
        ResourceBundle userN = ResourceBundle.getBundle("users");
        userN.keySet().forEach(key -> userData.put(Long.decode(key), userN.getString(key)));
        ResourceBundle friends = ResourceBundle.getBundle("friends");

        friends.keySet().forEach(key -> userFriends.put(
                Long.decode(key), Stream.of(friends.getString(key).split(",")).mapToLong(Long::parseLong).toArray()));

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
    public void updateUserInfo(@NotNull User user) {
        userData.put(user.id, user.username + "," + user.fullName + "," + user.email);
    }

    @Override
    public Collection<ShortUserInfo> listUsers(String partialName, int limit) {
        return userData.entrySet().stream().filter(idname -> idname.getValue().contains(partialName))
                .map(idname -> getUser(idname.getKey()))
                .map(User::shortInfo)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public @NotNull long[] getFriendIds(long id) {
        return ofNullable(userFriends.get(id)).orElse(new long[0]);
    }

    @Override
    public Collection<ShortUserInfo> listFriends(long currentUserId) {
        return Arrays.stream(getFriendIds(currentUserId)).mapToObj(this::getUser).map(User::shortInfo).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void addFriend(long id, Long friendId) {
        if (friendId == null) return;
        userFriends.put(id, arrayWithElement(userFriends.get(id), friendId));
    }

    @Override
    public void removeFriend(long id, Long friendId) {
        if (friendId == null) return;
        userFriends.put(id, arrayWithoutElement(userFriends.get(id), friendId));
    }

    private long[] arrayWithElement(long[] src, long el) {
        for (long l: src) if (l == el) return src;

        long[] newarr = Arrays.copyOf(src, src.length + 1);
        newarr[src.length] = el;
        return newarr;
    }

    private long[] arrayWithoutElement(long[] src, long el) {
        int srcl = src.length;
        for (int i = 0; i < srcl; i++) if (src[i] == el) {
            long[] newarr = new long[srcl - 1];
            System.arraycopy(src, 0, newarr, 0, i);
            System.arraycopy(src, i + 1, newarr, i, srcl - i - 1);
            return newarr;
        }
        return src;
    }

}
