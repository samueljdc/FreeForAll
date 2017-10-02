package me.angrypostman.freeforall.listeners;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserCache;
import me.angrypostman.freeforall.util.Configuration;
import me.angrypostman.freeforall.util.Message;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;

public class PlayerChatListener implements Listener{

    private FreeForAll plugin=null;
    private Configuration config=null;
    private Chat chat=null;

    public PlayerChatListener(FreeForAll plugin){
        this.plugin=plugin;
        this.config=plugin.getConfiguration();
        this.chat=plugin.getChat();
    }


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event){

        //If vault was not detected on server startup, or chat support is disabled
        //then ignore the chat event
        if(!plugin.hasVault() || config.isChatFormatting()) return;

        Player player=event.getPlayer();
        Optional<User> optional=UserCache.getUserIfPresent(player);
        if(!optional.isPresent()){
            player.sendMessage(ChatColor.RED+"Failed to load your player data, please relog.");
            event.setCancelled(true);
            return;
        }

        String content=event.getMessage();

        String group=chat.getPrimaryGroup(player);
        String groupPrefix=null;
        if (group!=null){
            groupPrefix=chat.getGroupPrefix(player.getWorld(), group);
        }

        Message message=Message.get("chat-format");
        message=message.replace("%group%", groupPrefix==null?(group == null?"":group):groupPrefix);
        message=message.replace("%player%", player.getName());
        message=message.replace("%content%", content);
        
        event.setMessage(message.getContent());

    }

}
