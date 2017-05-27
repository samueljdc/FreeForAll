package me.angrypostman.freeforall.listeners;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.*;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

import static me.angrypostman.freeforall.FreeForAll.doAsync;

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
        Optional<User> optional = UserManager.getUserIfPresent(player);

        if (!optional.isPresent()) return;

        User user = optional.get();
        UserData userData = user.getUserData();

        if (Combat.inCombat(user)) {

            Damage damage = Combat.getLastDamage(user);
            User attacker = damage.getDamager();
            UserData attackerData = attacker.getUserData();

            int playerLost = 50;

            attackerData.addPoints(playerLost);
            attackerData.addKill();

            userData.subtractPoints(playerLost);
            userData.addDeath();
            userData.endStreak();
            Combat.setLastDamage(user, null);

        }

        doAsync(() -> storage.saveUser(user));
        UserManager.getUsers().remove(user);

    }

}
