package me.angrypostman.freeforall.listeners;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.kit.FFAKit;
import me.angrypostman.freeforall.kit.KitManager;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserCache;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import static me.angrypostman.freeforall.FreeForAll.doSyncLater;

public class PlayerRespawnListener implements Listener{

    //Can't really think of anything else to do here tbh...

    public PlayerRespawnListener(final FreeForAll plugin){
        this.plugin=plugin;
        this.dataStorage=plugin.getDataStorage();
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerRespawn(final PlayerRespawnEvent event){

        final Player player=event.getPlayer();
        final Optional<User> optional=UserCache.getUserIfPresent(player);

        if(!optional.isPresent()){
            player.kickPlayer(ChatColor.RED+"Failed to load player data, please relog.");
            return;
        }

        final User user=optional.get();

        final List<Location> locations=this.dataStorage.getLocations();
        if(locations==null||locations.size()==0){ return; }

        final Random random=new Random();
        final int rand=random.nextInt(locations.size());

        final Location location=locations.get(rand);

        event.setRespawnLocation(location);

        doSyncLater(()->{

            Optional<FFAKit> kitOptional=KitManager.getKitOf(player);
            if(!kitOptional.isPresent()){
                kitOptional=KitManager.getDefaultKit();
                if(!kitOptional.isPresent()){ return; }
            }

            final FFAKit ffaKit=kitOptional.get();
            KitManager.giveItems(user, ffaKit);

            //A second later should be fine for this.
        }, 20L);
    }

    private final FreeForAll plugin;
    private final DataStorage dataStorage;
}
