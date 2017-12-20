package me.angrypostman.freeforall.user;

import com.google.common.base.Preconditions;
import java.util.*;
import java.util.concurrent.TimeUnit;
import me.angrypostman.freeforall.FreeForAll;
import org.bukkit.entity.Player;

public class Combat{

    public static Damage getLastDamage(final User user){
        return lastDamage.get(Preconditions.checkNotNull(user)
                                           .getUniqueId());
    }

    public static void setLastDamage(final User user,
                                     final Damage damage){
        Preconditions.checkNotNull(user, "user cannot be null");
        Preconditions.checkArgument(user.isOnline(), "user not online");
        lastDamage.put(user.getUniqueId(), damage);
    }

    public static boolean hasBeenInCombat(final User user){
        Preconditions.checkNotNull(user, "user cannot be null");
        Preconditions.checkArgument(user.isOnline(), "user not online");
        return lastDamage.get(user.getUniqueId())!=null;
    }

    public static boolean inCombat(final User user){
        final Damage damage=lastDamage.get(Preconditions.checkNotNull(user, "user")
                                                        .getUniqueId());
        return damage!=null&&System.currentTimeMillis()-damage.getTimestamp()<=TimeUnit.SECONDS.toMillis(
                plugin.getConfiguration()
                      .getPvPLoggerDuration());
    }

    public static boolean inCombat(final Player player){
        final Damage damage=lastDamage.get(Preconditions.checkNotNull(player, "player")
                                                        .getUniqueId());
        return damage!=null&&System.currentTimeMillis()-damage.getTimestamp()<=TimeUnit.SECONDS.toMillis(
                plugin.getConfiguration()
                      .getPvPLoggerDuration());
    }

    private static final Map<UUID, Damage> lastDamage=new HashMap<>();
    private static final List<UUID> invulnerables=new ArrayList<>();
    private static final FreeForAll plugin=FreeForAll.getPlugin();
}
