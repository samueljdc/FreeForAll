package me.angrypostman.freeforall.listeners;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.Combat;
import me.angrypostman.freeforall.user.Damage;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserManager;

import me.angrypostman.freeforall.util.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private FreeForAll plugin = null;
    private DataStorage storage = null;
    public PlayerDeathListener(FreeForAll plugin) {
        this.plugin = plugin;
        this.storage = plugin.getDataStorage();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();
        User user = UserManager.getUser(player);
        User killer = null;

        if (Combat.inCombat(user)) {

            Damage damage = Combat.getLastDamage(user);
            killer = damage.getDamagee();

            Combat.setLastDamage(user, null);

        }

        user.addDeath();
        user.endStreak();

        PlayerUtils.forceRespawn(user, null);

    }

}
