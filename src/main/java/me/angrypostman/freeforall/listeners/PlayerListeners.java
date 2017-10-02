package me.angrypostman.freeforall.listeners;

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

import java.util.List;

public class PlayerListeners implements Listener{

    private FreeForAll plugin=null;
    private Configuration config=null;

    public PlayerListeners(FreeForAll plugin){
        this.plugin=plugin;
        this.config=plugin.getConfiguration();
    }

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event){

    }

    @EventHandler
    public void onPlayerFoodLevelChange(FoodLevelChangeEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerExpChangeEvent(PlayerExpChangeEvent event){
        Player player=event.getPlayer();
        player.setExp(0);
        player.setTotalExperience(0);
        player.setLevel(0);
        event.setAmount(0);
    }

    @EventHandler
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event){

        Player player=event.getPlayer();
        String message=event.getMessage();
        String[] split=message.split(" ");
        if(split.length > 0) message=split[0];
        if(message.startsWith("/")) message=message.substring(1);

        PluginCommand command=plugin.getServer().getPluginCommand(message);

        if (!Combat.inCombat(player)){
            return;
        }

        List<String> bannedCommands=config.getBannedCommands();
        for(String bannedCommand : bannedCommands){
            if(bannedCommand.equalsIgnoreCase(command.getName())){
                Message.get("banned-command-message")
                        .replace("%command%", command.getName())
                        .send(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){

    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event){

    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event){

    }

}
