package me.angrypostman.freeforall.util;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Message{

    public Message(final String key){
        this.message=Preconditions.checkNotNull(messagesMap.get(key), "Message key cannot be null");
    }

    public static void load(final FileConfiguration configuration){
        messagesMap.clear();

        configuration.getConfigurationSection("messages")
                     .getKeys(false)
                     .forEach(message->{
                         messagesMap.put(message, ChatColor.translateAlternateColorCodes('&', configuration.getString(
                                 "messages."+message)));
                     });
    }

    public static Map<String, String> getMessagesMap(){
        return messagesMap;
    }

    public static Message get(final String key){
        return new Message(key);
    }

    public Message replace(final CharSequence replace,
                           final Object replacement){
        this.message=this.message.replace(replace, String.valueOf(replacement));
        return this;
    }

    public void send(final CommandSender sender){
        Preconditions.checkNotNull(sender, "sender cannot be null");
        sender.sendMessage(this.message);
    }

    public void send(final Entity entity){
        Preconditions.checkNotNull(entity, "entity cannot be null");
        if(entity instanceof Player){ Preconditions.checkArgument(((Player) entity).isOnline(), "player not online"); }
        entity.sendMessage(this.message);
    }

    @Override
    public String toString(){
        return "{message="+getContent()+"}";
    }

    public String getContent(){
        return this.message;
    }

    private static final Map<String, String> messagesMap=new HashMap<>();
    private String message=null;
}
