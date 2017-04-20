package me.angrypostman.freeforall;

import com.google.common.base.Charsets;
import me.angrypostman.freeforall.commands.KitCommand;
import me.angrypostman.freeforall.commands.SaveKitCommand;
import me.angrypostman.freeforall.commands.StatsCommand;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.data.MySQLStorage;
import me.angrypostman.freeforall.data.SQLiteStorage;
import me.angrypostman.freeforall.data.YamlStorage;
import me.angrypostman.freeforall.listeners.EntityDamageListener;
import me.angrypostman.freeforall.listeners.PlayerDeathListener;
import me.angrypostman.freeforall.listeners.PlayerLoginListener;
import me.angrypostman.freeforall.listeners.PlayerQuitListener;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserManager;
import me.angrypostman.freeforall.util.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

public class FreeForAll extends JavaPlugin {

    private static FreeForAll plugin = null;

    private DataStorage dataStorage = null;
    private Configuration configuration = null;

    public static FreeForAll getPlugin() {
        return plugin;
    }

    public DataStorage getDataStorage() {
        return dataStorage;
    }

    public static void doAsync(Runnable runnable) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public static void doAsyncLater(Runnable runnable, long ticksLater) {
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, ticksLater);
    }

    public static void doAsyncRepeating(Runnable runnable, long startAfterTicks, long repeatDelayTicks) {
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable,
                startAfterTicks, repeatDelayTicks);
    }

    public static void doSync(Runnable runnable) {
        Bukkit.getServer().getScheduler().runTask(plugin, runnable);
    }

    public static void doSyncLater(Runnable runnable, long ticksLater) {
        Bukkit.getScheduler().runTaskLater(plugin, runnable, ticksLater);
    }

    public static void doSyncRepeating(Runnable runnable, long startAfterTicks, long repeatDelayTicks) {
        Bukkit.getServer().getScheduler().runTaskTimer(plugin, runnable,
                startAfterTicks, repeatDelayTicks);
    }

    @Override
    public void onEnable() {

        plugin = this;

        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }

        try {
            YamlConfiguration config = new YamlConfiguration();
            config.load(new InputStreamReader(getResource("config.yml"), Charsets.UTF_8));
            validateConfig(config, getConfig());
            saveConfig();
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to validate configuration file.", e);
        }

        configuration = new Configuration(this);
        configuration.load();

        String storageMethod = configuration.getStorageMethod();
        if (storageMethod.equalsIgnoreCase("YAML") || storageMethod.equalsIgnoreCase("YML")) {
            File file = configuration.getYAMLDataFile();
            dataStorage = new YamlStorage(this, file);
        } else if (storageMethod.equalsIgnoreCase("MYSQL")) {
            dataStorage = new MySQLStorage(this, configuration.getSQLHost(),
                    configuration.getSQLDatabase(),
                    configuration.getSQLUser(),
                    configuration.getSQLPassword(),
                    configuration.getSQLPort());
        } else if (storageMethod.equalsIgnoreCase("SQLITE")) {
            File file = configuration.getSQLiteDataFile();
            dataStorage = new SQLiteStorage(this, file);
        } else {
            getLogger().info("Unknown storage method \"" + storageMethod + "\".");
            getLogger().info("Valid storage methods: YAML (YML), MYSQL, SQLITE");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Initializing data storage with storage method \"" + storageMethod.toUpperCase() + "\"...");
        dataStorage.initialize();

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new EntityDamageListener(this), this);
        manager.registerEvents(new PlayerDeathListener(this), this);
        manager.registerEvents(new PlayerLoginListener(this), this);
        manager.registerEvents(new PlayerQuitListener(this), this);

        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("kit").setExecutor(new KitCommand(this));
        getCommand("savekit").setExecutor(new SaveKitCommand(this));

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (onlinePlayers.size() > 0) {
            getLogger().info("Server reload detected, please refrain from doing this in the future as this can " +
                    "massively impair server performance.");
            getLogger().info("Attempting to load user data of " + onlinePlayers.size() + " players...");

            for (Player player : onlinePlayers) {
                UUID playerUUID = player.getUniqueId();
                User user = dataStorage.loadUser(playerUUID);

                if (user == null) {
                    user = dataStorage.createUser(playerUUID, player.getName());
                } else if (!user.getName().equals(player.getName())) {
                    user.setName(player.getName());
                    dataStorage.saveUser(user);
                }

                UserManager.getUsers().add(user);
            }
        }

    }

    @Override
    public void onDisable() {
        if (dataStorage != null) dataStorage.close();
        if (configuration != null) configuration.unload();

        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);

        plugin = null;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    private void validateConfig(ConfigurationSection from, ConfigurationSection to) {
        from.getKeys(true).stream().filter(fromKey -> !to.contains(fromKey)).forEachOrdered(fromKey -> {
            to.set(fromKey, from.get(fromKey));
        });
    }

}
