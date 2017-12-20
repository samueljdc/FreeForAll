package me.angrypostman.freeforall.util;

import org.bukkit.ChatColor;

public class StringUtils{

    public static String join(final String[] strings,
                              final char joiner){
        final StringBuilder builder=new StringBuilder();
        for(int i=0; i<strings.length; i++){
            if(i!=0){ builder.append(joiner); }
            builder.append(strings[i]);
        }
        return builder.toString();
    }

    public static String translateColorCodes(final String message){
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
