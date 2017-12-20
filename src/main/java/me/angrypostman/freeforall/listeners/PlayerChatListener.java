package me.angrypostman.freeforall.listeners;

import java.util.Optional;
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

public class PlayerChatListener implements Listener{

    public PlayerChatListener(final FreeForAll plugin){
        this.plugin=plugin;
        this.config=plugin.getConfiguration();
        this.chat=plugin.getChatManager();
    }

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event){

        //If vault was not detected on server startup, or chat support is disabled
        //then ignore the chat event
        if(this.config.isChatFormatting()||!this.plugin.hasVault()){ return; }

        final Player player=event.getPlayer();
        final Optional<User> optional=UserCache.getUserIfPresent(player);
        if(!optional.isPresent()){
            player.sendMessage(ChatColor.RED+"Failed to load your player data, please relog.");
            event.setCancelled(true);
            return;
        }

        final String content=event.getMessage();

        final String group=this.chat.getPrimaryGroup(player);
        String groupPrefix=null;
        if(group!=null){
            groupPrefix=this.chat.getGroupPrefix(player.getWorld(), group);
        }

        Message message=Message.get("chat-format");
        message=message.replace("%group%", groupPrefix==null ? (group==null ? "" : group) : groupPrefix);
        message=message.replace("%player%", player.getName());
        message=message.replace("%content%", content);

        event.setMessage(message.getContent());
    }

    private FreeForAll plugin=null;
    private Configuration config=null;
    private Chat chat=null;
}
