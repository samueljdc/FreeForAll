package me.angrypostman.freeforall.listeners;

import java.util.List;
import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.Combat;
import me.angrypostman.freeforall.util.Configuration;
import me.angrypostman.freeforall.util.Message;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

public class PlayerListeners implements Listener{

    public PlayerListeners(final FreeForAll plugin){
        this.plugin=plugin;
        this.config=plugin.getConfiguration();
    }

    @EventHandler
    public void onPlayerChatEvent(final AsyncPlayerChatEvent event){

    }

    @EventHandler
    public void onPlayerFoodLevelChange(final FoodLevelChangeEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerExpChangeEvent(final PlayerExpChangeEvent event){
        final Player player=event.getPlayer();
        player.setExp(0);
        player.setTotalExperience(0);
        player.setLevel(0);
        event.setAmount(0);
    }

    @EventHandler
    public void onPlayerPickupItemEvent(final PlayerPickupItemEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItemEvent(final PlayerDropItemEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerCommandPreProcess(final PlayerCommandPreprocessEvent event){

        final Player player=event.getPlayer();
        String message=event.getMessage();
        final String[] split=message.split(" ");
        if(split.length>0){ message=split[0]; }
        if(message.startsWith("/")){ message=message.substring(1); }

        final PluginCommand command=this.plugin.getServer()
                                               .getPluginCommand(message);

        if(!Combat.inCombat(player)){
            return;
        }

        final List<String> bannedCommands=this.config.getBannedCommands();
        for(final String bannedCommand : bannedCommands){
            if(bannedCommand.equalsIgnoreCase(command.getName())){
                Message.get("banned-command-message")
                       .replace("%command%", command.getName())
                       .send(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event){

    }

    @EventHandler
    public void onInteractEntity(final PlayerInteractEntityEvent event){

    }

    @EventHandler
    public void onInteractAtEntity(final PlayerInteractAtEntityEvent event){

    }

    private FreeForAll plugin=null;
    private Configuration config=null;
}
