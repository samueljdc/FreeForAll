package me.angrypostman.freeforall.listeners;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

public class PlayerJoinListener implements Listener {

    private FreeForAll plugin = null;
    private DataStorage dataStorage = null;

    public PlayerJoinListener(FreeForAll plugin) {
        this.plugin = plugin;
        this.dataStorage = plugin.getDataStorage();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        Optional<User> optional = UserManager.getUserIfPresent(player);

        if (!optional.isPresent()) {
            player.kickPlayer(ChatColor.RED + "Failed to load player data, please relog.");
            return;
        }


    }

}
