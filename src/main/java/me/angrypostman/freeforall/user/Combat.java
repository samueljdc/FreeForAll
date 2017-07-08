package me.angrypostman.freeforall.user;

import com.google.common.base.Preconditions;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Combat {

    private static Map<UUID, Damage> lastDamage = new HashMap<>();
    private static List<UUID> invulnerables = new ArrayList<>();
    ;

    public static Damage getLastDamage(User user) {
        return lastDamage.get(Preconditions.checkNotNull(user).getPlayerUUID());
    }

    public static void setLastDamage(User user, Damage damage) {
        Preconditions.checkNotNull(user, "user");
        lastDamage.put(user.getPlayerUUID(), damage);
    }

    public static boolean inCombat(User user) {
        Damage damage = lastDamage.get(Preconditions.checkNotNull(user, "user").getPlayerUUID());
        return damage != null
                && System.currentTimeMillis() - damage.getTimestamp() < TimeUnit.SECONDS.toMillis(10);
    }

    public static boolean isInvulnreble(User user) {
        return invulnerables.contains(Preconditions.checkNotNull(user, "user").getPlayerUUID());
    }

    public static void setInvulnreble(User user, boolean invulnerable) {
        Preconditions.checkNotNull(user, "user");
        if (invulnerable && !isInvulnreble(user)) {
            invulnerables.add(user.getPlayerUUID());
        } else {
            invulnerables.remove(user.getPlayerUUID());
        }
    }

}
