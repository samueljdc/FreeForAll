package me.angrypostman.freeforall.util;

import com.google.common.base.Preconditions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Message{

    private static Map<String, String> messagesMap=new HashMap<>();
    private String message=null;

    public Message(String key){
        this.message=Preconditions.checkNotNull(messagesMap.get(key), "Message key cannot be null");
    }

    public static void load(FileConfiguration configuration){
        messagesMap.clear();

        configuration.getConfigurationSection("messages").getKeys(false)
                .forEach(message -> {
            messagesMap.put(message, ChatColor
                    .translateAlternateColorCodes('&', configuration
                            .getString("messages."+message)));
        });

    }

    public static Map<String, String> getMessagesMap(){
        return messagesMap;
    }

    public static Message get(String key){
        return new Message(key);
    }

    public Message replace(CharSequence replace, Object replacement){
        message=message.replace(replace, String.valueOf(replacement));
        return this;
    }

    public void send(CommandSender sender){
        Preconditions.checkNotNull(sender, "sender cannot be null");
        sender.sendMessage(message);
    }

    public void send(Entity entity){
        Preconditions.checkNotNull(entity, "entity cannot be null");
        if (entity instanceof Player) Preconditions.checkArgument(((Player)entity).isOnline(),
                "player not online");
        entity.sendMessage(message);
    }

    public String getContent(){
        return message;
    }

    @Override
    public String toString(){
        return "{message=" + getContent() + "}";
    }
}
