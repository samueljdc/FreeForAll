package me.angrypostman.freeforall.user;

import com.google.common.base.Preconditions;
import me.angrypostman.freeforall.FreeForAll;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserManager {

    private static List<User> users = null;
    private static FreeForAll plugin = null;

    static {
        users = new ArrayList<>();
        plugin = FreeForAll.getPlugin();
    }

    public static Optional<User> getUser(Player player) {
        Preconditions.checkNotNull(player.getUniqueId(), "uuid cannot be null");
        return getUser(player.getUniqueId());
    }

    public static Optional<User> getUser(UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Optional<User> user = getUsers().stream().filter(u -> u.getPlayerUUID().equals(uuid)).findFirst();
        return user.isPresent() ? user : plugin.getDataStorage().loadUser(uuid);
    }

    public static Optional<User> getUser(String lookupName) {
        Preconditions.checkArgument(lookupName != null && lookupName.length() > 0, "lookupName cannnot be null or effectively null");
        Optional<User> user = getUsers().stream().filter(u -> u.getLookupName().equals(lookupName)).findFirst();
        return user.isPresent() ? user : plugin.getDataStorage().loadUser(lookupName);
    }

    public static Optional<User> getUserIfPresent(Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkArgument(player.isOnline(), "player not online");
        return getUserIfPresent(player.getUniqueId());
    }

    public static Optional<User> getUserIfPresent(UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        return getUsers().stream().filter(user -> user.getPlayerUUID().equals(uuid)).findFirst();
    }

    public static Optional<User> getUserIfPresent(String lookupName) {
        Preconditions.checkArgument(lookupName != null && lookupName.length() > 0, "lookupName cannot be null or effectively null");
        return getUsers().stream().filter(user -> user.getLookupName().equals(lookupName)).findFirst();
    }

    public static List<User> getUsers() {
        return users;
    }

}
