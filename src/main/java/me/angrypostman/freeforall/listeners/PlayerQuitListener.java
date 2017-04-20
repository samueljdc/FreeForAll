package me.angrypostman.freeforall.listeners;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.Combat;
import me.angrypostman.freeforall.user.Damage;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private FreeForAll plugin = null;
    private DataStorage storage = null;
    public PlayerQuitListener(FreeForAll plugin) {
        this.plugin = plugin;
        this.storage = plugin.getDataStorage();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        User user = UserManager.getUser(player);

        if (user.hasKillStreak()) {
            

        }

        if (Combat.inCombat(user)) {

            Damage damage = Combat.getLastDamage(user);
            User attacker = damage.getDamager();

            int playerLost = 50;

            attacker.addPoints(playerLost);
            attacker.addKill();

            user.subtractPoints(playerLost);
            user.addDeath();
            user.endStreak();
            Combat.setLastDamage(user, null);

        }

        storage.saveUser(user);
        UserManager.getUsers().remove(user);

    }

}
