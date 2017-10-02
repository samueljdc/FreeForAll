package me.angrypostman.freeforall.user;

import com.google.common.base.Preconditions;
import me.angrypostman.freeforall.FreeForAll;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserCache{

    private static List<User> users=null;
    private static List<User> spectators=null;
    private static FreeForAll plugin=null;

    static{
        users=new ArrayList<>();
        spectators=new ArrayList<>();
        plugin=FreeForAll.getPlugin();
    }

    public static Optional<User> getUser(Player player){
        Preconditions.checkNotNull(player, "player cannot be null");
        return getUser(player.getUniqueId());
    }

    public static Optional<User> getUser(UUID uuid){
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Optional<User> user=users.stream().filter(u -> u.getUniqueId().equals(uuid)).findFirst();
        return user.isPresent() ? user : plugin.getDataStorage().loadUser(uuid);
    }

    public static Optional<User> getUser(String lookupName){
        Preconditions.checkArgument(lookupName != null && lookupName.length() > 0, "lookupName cannnot be null or effectively null");
        Optional<User> user=users.stream().filter(u -> u.getLookupName().equals(lookupName)).findFirst();
        return user.isPresent() ? user : plugin.getDataStorage().loadUser(lookupName);
    }

    public static Optional<User> getUserIfPresent(Player player){
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkArgument(player.isOnline(), "player not online");
        return getUserIfPresent(player.getUniqueId());
    }

    public static Optional<User> getUserIfPresent(UUID uuid){
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        return users.stream().filter(user -> user.getUniqueId().equals(uuid)).findFirst();
    }

    public static Optional<User> getUserIfPresent(String lookupName){
        Preconditions.checkArgument(lookupName != null && lookupName.length() > 0, "lookupName cannot be null or effectively null");
        return users.stream().filter(user -> user.getLookupName().equals(lookupName)).findFirst();
    }

    public static boolean isSpectating(User user){
        Preconditions.checkNotNull(user, "user");
        Preconditions.checkArgument(user.isOnline(), "user not online");
        return isSpectating(user.getBukkitPlayer());
    }

    public static boolean isSpectating(Player player){
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkArgument(player.isOnline(), "player not online");
        return isSpectating(player.getUniqueId());
    }

    public static boolean isSpectating(UUID uuid){
        Preconditions.checkNotNull(uuid, "uuid");
        return spectators.stream().anyMatch(user -> user.getUniqueId().equals(uuid));
    }

    public static List<User> getSpectators(){
        return new ArrayList<>(spectators);
    }

    public static void setSpectating(User user, boolean spectating){
        if(spectating){
            if(!isSpectating(user)){
                spectators.add(user);
            }
        }else if(isSpectating(user)){
            spectators.remove(user);
        }
    }

    public static List<User> getUsers(){
        return new ArrayList<>(users);
    }

    public static void cacheUser(User user){
        Preconditions.checkNotNull(user, "user cannot be null");

        //Expire existing data
        expireUser(user);
        users.add(user);
    }

    public static void expireUser(User user){
        Preconditions.checkNotNull(user, "user cannot be null");
        users.remove(user);
    }

}
