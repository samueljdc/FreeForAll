package me.angrypostman.freeforall.user;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.angrypostman.freeforall.FreeForAll;
import org.bukkit.entity.Player;

public class UserCache{

    static{
        users=new ArrayList<>();
        spectators=new ArrayList<>();
        plugin=FreeForAll.getPlugin();
    }

    public static Optional<User> getUser(final Player player){
        Preconditions.checkNotNull(player, "player cannot be null");
        return getUser(player.getUniqueId());
    }

    public static Optional<User> getUser(final UUID uuid){
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        final Optional<User> user=users.stream()
                                       .filter(u->u.getUniqueId()
                                                   .equals(uuid))
                                       .findFirst();
        return user.isPresent() ? user : plugin.getDataStorage()
                                               .loadUser(uuid);
    }

    public static Optional<User> getUser(final String lookupName){
        Preconditions.checkArgument(lookupName!=null&&lookupName.length()>0,
                                    "lookupName cannnot be null or effectively null");
        final Optional<User> user=users.stream()
                                       .filter(u->u.getLookupName()
                                                   .equals(lookupName))
                                       .findFirst();
        return user.isPresent() ? user : plugin.getDataStorage()
                                               .loadUser(lookupName);
    }

    public static Optional<User> getUserIfPresent(final Player player){
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkArgument(player.isOnline(), "player not online");
        return getUserIfPresent(player.getUniqueId());
    }

    public static Optional<User> getUserIfPresent(final UUID uuid){
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        return users.stream()
                    .filter(user->user.getUniqueId()
                                      .equals(uuid))
                    .findFirst();
    }

    public static Optional<User> getUserIfPresent(final String lookupName){
        Preconditions.checkArgument(lookupName!=null&&lookupName.length()>0,
                                    "lookupName cannot be null or effectively null");
        return users.stream()
                    .filter(user->user.getLookupName()
                                      .equals(lookupName))
                    .findFirst();
    }

    public static List<User> getSpectators(){
        return new ArrayList<>(spectators);
    }

    public static void setSpectating(final User user,
                                     final boolean spectating){
        if(spectating){
            if(!isSpectating(user)){ //No duplicate entries in spectator list
                spectators.add(user);
            }
        }else if(isSpectating(user)){
            spectators.remove(user);
        }
    }

    public static boolean isSpectating(final User user){
        Preconditions.checkNotNull(user, "user");
        Preconditions.checkArgument(user.isOnline(), "user not online");
        return isSpectating(user.getBukkitPlayer());
    }

    public static boolean isSpectating(final Player player){
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkArgument(player.isOnline(), "player not online");
        return isSpectating(player.getUniqueId());
    }

    public static boolean isSpectating(final UUID uuid){
        Preconditions.checkNotNull(uuid, "uuid");
        return spectators.stream()
                         .anyMatch(user->user.getUniqueId()
                                             .equals(uuid));
    }

    public static List<User> getUsers(){
        return new ArrayList<>(users);
    }

    public static void cacheUser(final User user){
        Preconditions.checkNotNull(user, "user cannot be null");

        //Expire existing data
        expireUser(user);
        users.add(user);
    }

    public static void expireUser(final User user){
        Preconditions.checkNotNull(user, "user cannot be null");
        UserCache.users.remove(user);
        UserCache.spectators.remove(user);
    }

    private static List<User> users=null;
    private static List<User> spectators=null;
    private static FreeForAll plugin=null;
}
