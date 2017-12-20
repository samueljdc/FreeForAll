package me.angrypostman.freeforall.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.angrypostman.freeforall.FreeForAll;
import org.bukkit.configuration.file.FileConfiguration;

public class Configuration{

    private final Map<String, Object> values=new HashMap<>();

    public Configuration(final FreeForAll plugin){
        this.plugin=plugin;
        this.configuration=plugin.getConfig();
    }

    public FileConfiguration getFileConfiguration(){
        return this.configuration;
    }

    public void load(){

        this.values.clear();

        this.values.put("storage.storageMethod", this.configuration.getString("storage.storageMethod"));
        this.values.put("storage.yaml.dataFile", this.configuration.getString("storage.yaml.dataFile"));
        this.values.put("storage.mysql.host", this.configuration.getString("storage.mysql.host"));
        this.values.put("storage.mysql.database", this.configuration.getString("storage.mysql.database"));
        this.values.put("storage.mysql.username", this.configuration.getString("storage.mysql.username"));
        this.values.put("storage.mysql.password", this.configuration.getString("storage.mysql.password"));
        this.values.put("storage.mysql.port", this.configuration.getInt("storage.mysql.port"));
        this.values.put("storage.sqlite.dataFile", this.configuration.getString("storage.sqlite.dataFile"));

        this.values.put("settings.debugMode", this.configuration.getBoolean("settings.debugMode"));

        this.values.put("settings.combat.pvpLogger", this.configuration.getBoolean("settings.combat.pvpLogger"));
        this.values.put("settings.combat.disablePlayerCollision",
                        this.configuration.getBoolean("settings.combat.disablePlayerCollision"));
        this.values.put("settings.combat.pvpLoggerDuration",
                        this.configuration.getInt("settings.combat.pvpLoggerDuration"));
        this.values.put("settings.combat.gainedLost", this.configuration.getString("settings.combat.gainedLost"));

        this.values.put("settings.combat.bannedCommands",
                        this.configuration.getStringList("settings.combat.bannedCommands"));
        this.values.put("settings.chat.chatFormatting", this.configuration.getBoolean("settings.chat.chatFormatting"));
        this.values.put("settings.chat.chatFormat", this.configuration.getStringList("settings.chat.chatFormat"));

        this.values.put("settings.defaultKit", this.configuration.getString("settings.defaultKit"));
        this.values.put("version", this.configuration.getString("version"));
    }

    public void saveConfiguration(){
        this.values.forEach((entry, value)->this.configuration.set(entry, value));
        this.plugin.saveConfig();
    }

    public void unload(){
        this.values.clear();
    }

    public Object get(final String path){
        return this.values.get(path);
    }

    public void set(final String path,
                    final Object value){
        this.values.put(path, value);
    }

    public String getDefaultKit(){
        return (String) this.values.get("settings.defaultKit");
    }

    public boolean isPvPLogger(){
        return getPvPLoggerDuration()>0;
    }

    public int getPvPLoggerDuration(){
        return (int) this.values.get("settings.combat.pvpLoggerDuration");
    }

    public String getGainedLost(){
        return (String) this.values.get("settings.combat.gainedLost");
    }

    public List<String> getBannedCommands(){
        return (List<String>) this.values.get("settings.combat.bannedCommands");
    }

    public boolean isChatFormatting(){
        return (boolean) this.values.get("settings.chat.chatFormatting");
    }

    public String getChatFormat(){
        return (String) this.values.get("settings.chat.chatFormat");
    }

    public boolean isDebugMode(){
        return (boolean) this.values.get("settings.debugMode");
    }

    public String getVersion(){
        return (String) this.values.get("version");
    }

    public String getStorageMethod(){
        return (String) this.values.get("storage.storageMethod");
    }

    public File getYAMLDataFile(){
        return new File(this.plugin.getDataFolder(), (String) this.values.get("storage.yaml.dataFile"));
    }

    public String getSQLHost(){
        return (String) this.values.get("storage.mysql.host");
    }

    public String getSQLDatabase(){
        return (String) this.values.get("storage.mysql.database");
    }

    public String getSQLUser(){
        return (String) this.values.get("storage.mysql.username");
    }

    public String getSQLPassword(){
        return (String) this.values.get("storage.mysql.password");
    }

    public int getSQLPort(){
        return (int) this.values.get("storage.mysql.port");
    }

    public File getSQLiteDataFile(){
        return new File(this.plugin.getDataFolder(), (String) this.values.get("storage.sqlite.dataFile"));
    }

    private FreeForAll plugin=null;
    private FileConfiguration configuration=null;
}
