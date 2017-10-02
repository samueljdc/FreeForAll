package me.angrypostman.freeforall.user;

import com.google.common.base.Preconditions;
import me.angrypostman.freeforall.FreeForAll;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Combat{

    private static Map<UUID, Damage> lastDamage=new HashMap<>();
    private static List<UUID> invulnerables=new ArrayList<>();
    private static FreeForAll plugin=FreeForAll.getPlugin();

    public static Damage getLastDamage(User user){
        return lastDamage.get(Preconditions.checkNotNull(user).getUniqueId());
    }

    public static void setLastDamage(User user, Damage damage){
        Preconditions.checkNotNull(user, "user cannot be null");
        Preconditions.checkArgument(user.isOnline(), "user not online");
        lastDamage.put(user.getUniqueId(), damage);
    }

    public static boolean hasBeenInCombat(User user){
        Preconditions.checkNotNull(user, "user cannot be null");
        Preconditions.checkArgument(user.isOnline(), "user not online");
        return lastDamage.get(user.getUniqueId())!=null;
    }

    public static boolean inCombat(User user){
        Damage damage=lastDamage.get(Preconditions.checkNotNull(user, "user").getUniqueId());
        return damage != null && System.currentTimeMillis() - damage.getTimestamp() <= TimeUnit.SECONDS
                .toMillis(plugin.getConfiguration().getPvPLoggerDuration());
    }

    public static boolean inCombat(Player player){
        Damage damage=lastDamage.get(Preconditions.checkNotNull(player, "player").getUniqueId());
        return damage != null && System.currentTimeMillis() - damage.getTimestamp() <= TimeUnit.SECONDS
                .toMillis(plugin.getConfiguration().getPvPLoggerDuration());
    }

}
