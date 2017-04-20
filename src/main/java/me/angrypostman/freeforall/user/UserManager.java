package me.angrypostman.freeforall.user;

import com.google.common.base.Preconditions;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserManager {

    private static List<User> users = new ArrayList<>();;

    public static User getUser(Player player) {
        return getUser(Preconditions.checkNotNull(player).getUniqueId());
    }

    public static User getUser(UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        for (User player : getUsers()) {
            if (player.getPlayerUUID().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    public static User getUser(String lookupName) {
        Preconditions.checkNotNull(lookupName, "lookupName");
        for (User user : getUsers()) {
            if (user.getLookupName().equals(lookupName)) {
                return user;
            }
        }
        return null;
    }

    public static List<User> getUsers() {
        return users;
    }

}
