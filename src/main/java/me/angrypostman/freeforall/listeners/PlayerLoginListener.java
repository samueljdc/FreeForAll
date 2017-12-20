package me.angrypostman.freeforall.listeners;

import java.util.Optional;
import java.util.UUID;
import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PlayerLoginListener implements Listener{

    public PlayerLoginListener(final FreeForAll plugin){
        this.plugin=plugin;
        this.storage=plugin.getDataStorage();
    }

    @EventHandler
    public void onAsyncPreLogin(final AsyncPlayerPreLoginEvent event){

        if(event.getLoginResult()!=AsyncPlayerPreLoginEvent.Result.ALLOWED){
            this.plugin.getLogger()
                       .info("Player "+event.getName()+"("+event.getUniqueId()+") was denied access during login, "+"ignoring this player.");
            return;
        }

        final String playerName=event.getName();
        final UUID playerUUID=event.getUniqueId();

        Optional<User> optional=this.storage.loadUser(playerUUID);
        if(!optional.isPresent()){
            optional=this.storage.createUser(playerUUID, playerName);
            if(!optional.isPresent()){
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                               "Failed to generate player data, please relog");
                this.plugin.getLogger()
                           .info("An error occurred whilst generating player data for '"+playerName+"'");
                return;
            }
        }

        final User user=optional.get();
        if(!user.getName()
                .equals(playerName)){
            user.setName(playerName);
            this.storage.saveUser(user);
        }

        UserCache.cacheUser(user);
    }

    private FreeForAll plugin=null;
    private DataStorage storage=null;
}
