package me.angrypostman.freeforall.listeners;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.*;

import me.angrypostman.freeforall.util.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Optional;

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
        Optional<User> optional = UserManager.getUserIfPresent(player);

        if (!optional.isPresent()) { //This should NEVER happen
            player.kickPlayer(ChatColor.RED+"Failed to load player data, please relog");
            return;
        }

        User user = optional.get();
        User killer = null;

        if (Combat.inCombat(user)) {

            Damage damage = Combat.getLastDamage(user);
            killer = damage.getDamagee();

            Combat.setLastDamage(user, null);

        }

        UserData userData = user.getUserData();

        userData.addDeath();
        userData.endStreak();

        World world = player.getWorld();

        PlayerUtils.forceRespawn(user, world.getSpawnLocation());

    }

}
