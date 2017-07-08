package me.angrypostman.freeforall.listeners;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Optional;

public class PlayerMoveListener implements Listener {

    private FreeForAll plugin = null;
    private DataStorage dataStorage = null;

    public PlayerMoveListener(FreeForAll plugin) {
        this.plugin = plugin;
        this.dataStorage = plugin.getDataStorage();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        Optional<User> optional = UserManager.getUserIfPresent(player);

        if (!optional.isPresent()) {
            event.setCancelled(true);
            return;
        }

        User user = optional.get();

    }


}
