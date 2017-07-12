package me.angrypostman.freeforall.util;

import me.angrypostman.freeforall.FreeForAll;
import org.bukkit.WeatherType;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Configuration {

    private FreeForAll plugin = null;
    private FileConfiguration configuration = null;
    private Map<String, Object> values = new HashMap<>();

    public Configuration(FreeForAll plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfig();
    }

    public FileConfiguration getFileConfiguration() {
        return configuration;
    }

    public void load() {

        values.clear();

        values.put("storage.storageMethod", configuration.getString("storage.storageMethod"));
        values.put("storage.yaml.dataFile", configuration.getString("storage.yaml.dataFile"));
        values.put("storage.mysql.host", configuration.getString("storage.mysql.host"));
        values.put("storage.mysql.database", configuration.getString("storage.mysql.database"));
        values.put("storage.mysql.username", configuration.getString("storage.mysql.username"));
        values.put("storage.mysql.password", configuration.getString("storage.mysql.password"));
        values.put("storage.mysql.port", configuration.getInt("storage.mysql.port"));
        values.put("storage.sqlite.dataFile", configuration.getString("storage.sqlite.dataFile"));
        values.put("settings.combat.disablePlayerCollision", configuration.getBoolean("settings.combat.disablePlayerCollision"));
        values.put("settings.combat.playerRespawnImmunity", configuration.getInt("settings.combat.playerRespawnImmunity"));
        values.put("settings.combat.pvpLogger", configuration.getBoolean("settings.combat.pvpLogger"));
        values.put("settings.combat.pvpLoggerDuration", configuration.getInt("settings.combat.pvpLoggerDuration"));
        values.put("settings.combat.gainedLost", configuration.getInt("settings.combat.gainedLost"));
        values.put("settings.world.disableWeatherChange", configuration.getBoolean("settings.world.disableWeatherChange"));
        values.put("settings.world.disableBlockPlace", configuration.getBoolean("settings.world.disableBlockPlace"));
        values.put("settings.world.disableBlockDestroy", configuration.getBoolean("settings.world.disableBlockDestroy"));
        values.put("settings.world.disableCreatureSpawn", configuration.getBoolean("settings.world.disableCreatureSpawn"));
        values.put("settings.world.disableExplosionDestroy", configuration.getBoolean("settings.world.disableExplosionDestroy"));
        values.put("settings.defaultKit", configuration.getString("settings.defaultKit"));
        values.put("version", configuration.getString("version"));

    }

    public void saveConfiguration() {
        values.forEach((entry, value) -> configuration.set(entry, value));
        plugin.saveConfig();
    }

    public void unload() {
//        plugin.getLogger().info("Unloading configuration values from memory...");
//        values.clear();
//        plugin.getLogger().info("Configuration values unloaded from system memory...");
    }

    public Object get(String path) {
        return values.get(path);
    }

    public void set(String path, Object value) {
        values.put(path, value);
    }

    public String getDefaultKit() {
        return (String) values.get("settings.defaultKit");
    }

    public boolean disablePVPLogger() {
        return (!(boolean)values.get("settings.combat.pvpLogger"));
    }

    public boolean disableWeatherChange() {
        return (boolean) values.get("settings.world.disableWeatherChange");
    }

    public WeatherType getDefaultWeatherType() {
        return (WeatherType)values.get("settings.world.defaultWeatherType");
    }

    public boolean disableBlockPlace() {
        return (boolean)values.get("settings.world.disableBlockPlace");
    }

    public boolean disableBlockDestroy() {
        return (boolean)values.get("settings.world.disableBlockDestroy");
    }

    public boolean disableCreatureSpawn() {
        return (boolean)values.get("settings.world.disableCreatureSpawn");
    }

    public boolean disableExplosionDestroy() {
        return (boolean) values.get("settings.world.disableExplosionDestroy");
    }

    public boolean disablePlayerCollision() {
        return (boolean) values.get("settings.combat.disablePlayerCollision");
    }


    public boolean enablePvpLogger() {
        return (boolean) values.get("settings.combat.pvpLogger");
    }

    public long pvpLoggerDuration() {
        return (long) values.get("settings.combat.pvpLoggerDuration") * 1000L;
    }

    public int getGainedLost() {
        return (int) values.get("settings.combat.gainedLost");
    }

    public String getVersion() {
        return (String) values.get("version");
    }

    public String getStorageMethod() {
        return (String) values.get("storage.storageMethod");
    }

    public File getYAMLDataFile() {
        return new File(plugin.getDataFolder(), (String) values.get("storage.yaml.dataFile"));
    }

    public String getSQLHost() {
        return (String) values.get("storage.mysql.host");
    }

    public String getSQLDatabase() {
        return (String) values.get("storage.mysql.database");
    }

    public String getSQLUser() {
        return (String) values.get("storage.mysql.username");
    }

    public String getSQLPassword() {
        return (String) values.get("storage.mysql.password");
    }

    public int getSQLPort() {
        return (int) values.get("storage.mysql.port");
    }

    public File getSQLiteDataFile() {
        return new File(plugin.getDataFolder(), (String) values.get("storage.sqlite.dataFile"));
    }

}
