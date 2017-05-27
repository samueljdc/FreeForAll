package me.angrypostman.freeforall;

import com.google.common.base.Charsets;
import me.angrypostman.freeforall.commands.*;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.data.MySQLStorage;
import me.angrypostman.freeforall.data.SQLiteStorage;
import me.angrypostman.freeforall.data.YamlStorage;
import me.angrypostman.freeforall.listeners.*;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserManager;
import me.angrypostman.freeforall.util.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class FreeForAll extends JavaPlugin {

    /*
         1. Data Storage > SQLite and YML data storage implementation
         2. Commands > Set spawn (multiple) (use data storage or local yml?)
         3. Commands > Del spawn
         4. EntityDamageListener > Spawned creatures that attack a player still trigger the combat tag
         5. PlayerDeathListener > Reward killer (increment points, kills, killStreak, anything else?), subtract
         points from the killed user, end kill streak, anything else?
         6. PlayerJoinListener > Not sure yet
         7. PlayerQuitListener > Calculate lost points, other stuff
         8. PlayerMoveListener > Not sure yet (boundaries for the arena ?)
         9. Implement stats (arrows shot, neutral monsters killed, etc) at some point, probably
         after initial release
         10. Kits > Load kits,
         11. Optional chat formatting at some point (after release)
         12. Implement player ranking
         13. Commands > Reset stats
         14. Commands > Leaderboard (/leaderboard [page] (aliases: /top))
     */

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
            getLogger().info("Failed to find configuration, saving default config...");
            saveDefaultConfig();
        }

        getLogger().info("Validating configuration file...");
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
            String host = configuration.getSQLHost();
            String database = configuration.getSQLDatabase();
            String username = configuration.getSQLUser();
            String password = configuration.getSQLPassword();
            Integer port = configuration.getSQLPort();
            dataStorage = new MySQLStorage(this, host, database, username, password, port);
        } else if (storageMethod.equalsIgnoreCase("SQLITE")) {
            File file = configuration.getSQLiteDataFile();
            dataStorage = new SQLiteStorage(this, file);
        } else {
            getLogger().info("Unknown storage method \"" + storageMethod + "\".");
            getLogger().info("Valid storage methods include: YAML (or YML), MYSQL, SQLITE");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Initializing data storage with storage method \"" + storageMethod.toUpperCase() + "\"...");
        if(!dataStorage.initialize()) {
            String message = "Failed to initialize data storage, please check the logs for further details.";
            Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(player -> player.sendMessage("[FreeForAll] "+message));
            getPluginLoader().disablePlugin(this);
            return;
        }

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new EntityDamageListener(this), this);
        manager.registerEvents(new PlayerDeathListener(this), this);
        manager.registerEvents(new PlayerLoginListener(this), this);
        manager.registerEvents(new PlayerJoinListener(this), this);
        manager.registerEvents(new PlayerQuitListener(this), this);
        manager.registerEvents(new GeneralListeners(this), this);

        //Might convert to using custom command handler at some point,
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("resetstats").setExecutor(new ResetStatsCommand(this));
        getCommand("kit").setExecutor(new KitCommand(this));
        getCommand("savekit").setExecutor(new SaveKitCommand(this));
//        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
//        getCommand("delspawn").setExecutor(new DelSpawnCommand(this));

        int online = Bukkit.getOnlinePlayers().size();
        if (online > 0) {
            getLogger().info("Server reload detected, please refrain from doing this in the future as this can " +
                    "massively impair server performance.");
            getLogger().info("Attempting to load user data of " + online + " players, this may take a while...");

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID playerUUID = player.getUniqueId();
                Optional<User> optional = dataStorage.loadUser(playerUUID);

                if (!optional.isPresent()) { //Should always be false
                    optional = dataStorage.createUser(playerUUID, player.getName());
                    if (!optional.isPresent()) {
                        player.kickPlayer(ChatColor.RED+"Failed to generate player data, please relog.");
                        continue;
                    }
                }

                User user = optional.get();
                if (!user.getName().equals(player.getName())) {
                    user.setName(player.getName());
                }

                UserManager.getUsers().add(user);
            }

            getLogger().info("Loaded data of "+UserManager.getUsers().size()+"/"+online+" players.");

        }

        doSyncRepeating(() -> {

            //Cleanup unused data
            Iterator<User> iterator = UserManager.getUsers().iterator();
            while (iterator.hasNext()) {

                User user = iterator.next();

                Player player = user.getBukkitPlayer();
                long downloadTime = user.getDownloadTime();

                if (player == null || !player.isOnline()) {
                    if (System.currentTimeMillis() - downloadTime >= TimeUnit.MINUTES.toMillis(10)) {
                        iterator.remove();
                    }
                }

            }

            //Execute this task every 10 minutes
            //startAfterTicks = 60 * 10 * 20 (10 minutes), repeatDelayTicks = 60 * 10 * 20 (10 minutes)
        }, 60 * 10 * 20, 60 * 10 * 20); //Every 10 minutes repeat this task

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
