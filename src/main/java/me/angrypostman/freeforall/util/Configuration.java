package me.angrypostman.freeforall.util;

import me.angrypostman.freeforall.FreeForAll;
import org.bukkit.WeatherType;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
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

        values.put("storage.method", configuration.getString("storage.storageMethod"));
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
        values.put("settings.defaultKit", configuration.getString("settings.defaultKit"));
        values.put("version", configuration.getDouble("version"));

    }

    public void save() {
        values.forEach((entry, value) -> configuration.set(entry, value));
        plugin.saveConfig();
    }

    public void unload() {
        values.clear();
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

    public boolean disablePlayerCollision() {
        return (boolean) values.get("settings.combat.disablePlayerCollision");
    }

    public boolean enablePvpLogger() { return (boolean) values.get("settings.combat.pvpLogger"); }

    public long pvpLoggerDuration() { return (long) values.get("settings.combat.pvpLoggerDuration") * 1000L; }

    public boolean disableWeather() { return (boolean) values.get("settings.world.disableWeather"); }

    public boolean disablePlayerDrop() { return (boolean) values.get("settings.world.disablePlayerDrop"); }

    public boolean disablePlayerPickup() { return (boolean) values.get("settings.world.disablePlayerPickup"); }

    public boolean disableCreatures() { return (boolean) values.get("settings.world.disableCreatures"); }

    public double getVersion() {
        return (double) values.get("version");
    }

    public String getStorageMethod() {
        return (String) values.get("storage.method");
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
