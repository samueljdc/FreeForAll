package me.angrypostman.freeforall.listeners;

import java.util.Optional;
import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener{

    public PlayerMoveListener(final FreeForAll plugin){
        this.plugin=plugin;
        this.dataStorage=plugin.getDataStorage();
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event){

        final Player player=event.getPlayer();
        final Optional<User> optional=UserCache.getUserIfPresent(player);

        if(!optional.isPresent()){
            event.setCancelled(true);
            return;
        }

        final User user=optional.get();
    }

    private FreeForAll plugin=null;
    private DataStorage dataStorage=null;
}
