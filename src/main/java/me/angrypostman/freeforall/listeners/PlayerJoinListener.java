package me.angrypostman.freeforall.listeners;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserCache;
import me.angrypostman.freeforall.util.Message;
import me.angrypostman.freeforall.util.Updater;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.Optional;

import static me.angrypostman.freeforall.FreeForAll.doAsync;

public class PlayerJoinListener implements Listener{

    private FreeForAll plugin=null;
    private DataStorage dataStorage=null;

    public PlayerJoinListener(FreeForAll plugin){
        this.plugin=plugin;
        this.dataStorage=plugin.getDataStorage();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){

        Player player=event.getPlayer();
        Optional<User> optional=UserCache.getUserIfPresent(player);

        if(!optional.isPresent()){
            player.kickPlayer(ChatColor.RED + "Failed to load player data, please relog.");
            return;
        }

        User user=optional.get();

        PlayerInventory inventory=player.getInventory();
        inventory.clear();
        inventory.setArmorContents(null);

        player.setGameMode(GameMode.SURVIVAL);
        player.setFoodLevel(20);
        player.setHealth(getMaxHealth(player));
        player.setFireTicks(0);
        player.setLevel(0);
        player.setExp(0);
        player.setTotalExperience(0);

        Message message=Message.get("join-message-broadcast");
        if(message.getContent() != null && !message.getContent().isEmpty()){
            message=message.replace("%player%", user.getName());
            event.setJoinMessage(message.getContent());
        }

        if (player.hasPermission("freeforall.viewupdates")){
            doAsync(() -> {

                Updater updater=plugin.getUpdater();

                String latestVersion=updater.getLatestVersion();
                String latestVersionURL=updater.getLatestVersionURL();
                if(latestVersion != null){
                    player.sendMessage(ChatColor.GREEN+"A new version of FreeForAll is available for download (v" + latestVersion + ")!");
                }

            });
        }

    }

    private double getMaxHealth(Player player){
        try{
            return player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        } catch(NoClassDefFoundError ex){
            return player.getMaxHealth();
        }
    }

}
