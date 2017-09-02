package me.angrypostman.freeforall.util;

import org.bukkit.ChatColor;

public class StringUtils{

    public static String join(String[] strings, char joiner){
        StringBuilder builder=new StringBuilder();
        for(int i=0; i < strings.length; i++){
            if(i != 0) builder.append(joiner);
            builder.append(strings[i]);
        }
        return builder.toString();
    }


    public static String translateColorCodes(String message){
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
