package me.angrypostman.freeforall;

import com.google.common.base.Charsets;
import me.angrypostman.freeforall.commands.*;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.data.MySQLStorage;
import me.angrypostman.freeforall.data.SQLiteStorage;
import me.angrypostman.freeforall.data.YamlStorage;
import me.angrypostman.freeforall.kit.KitManager;
import me.angrypostman.freeforall.listeners.*;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserManager;
import me.angrypostman.freeforall.util.Configuration;
import me.angrypostman.freeforall.util.Updater;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class FreeForAll extends JavaPlugin {

    /*
         1. Data Storage > SQLite and YML data storage implementation
         6. PlayerJoinListener > Not sure yet
         7. PlayerQuitListener > Calculate lost points, other stuff
         11. Optional chat formatting at some point (after release)
         12. Implement player ranking
         15. Implement customizable messages
         17. Kill rewards (customizable), potion effects?
     */

    private static FreeForAll plugin;

    private DataStorage dataStorage = null;
    private Configuration configuration = null;

    //We want every Runnable that we schedule to be logged
    //So we can later forcibly run them on shutdown
    private List<BukkitRunnable> runnables = new ArrayList<>();

    public static FreeForAll getPlugin() {
        return plugin;
    }

    public static void doAsync(Runnable runnable) {
        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable ignored) { }
            }
        };
        bukkitRunnable.runTaskAsynchronously(plugin);
    }

    public static void doAsyncLater(Runnable runnable, long ticksLater) {
        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable ignored) {}

                //Make sure that the task gets cancelled no matter what
                plugin.cancelTask(this.getTaskId());
            }
        };
        plugin.getRunnables().add(bukkitRunnable);
        bukkitRunnable.runTaskLaterAsynchronously(plugin, ticksLater);
    }

    public static void doAsyncRepeating(Runnable runnable, long startAfterTicks, long repeatDelayTicks) {
        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable ignored) {
                    plugin.cancelTask(this.getTaskId());
                }
            }
        };
        plugin.getRunnables().add(bukkitRunnable);
        bukkitRunnable.runTaskTimerAsynchronously(plugin, startAfterTicks, repeatDelayTicks);
    }

    public static void doSync(Runnable runnable) {
        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable ignored) {}
            }
        };
        bukkitRunnable.runTask(plugin);
    }

    public static void doSyncLater(Runnable runnable, long ticksLater) {
        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable ignored) {}

                //Make sure that the task gets cancelled no matter what
                plugin.cancelTask(this.getTaskId());
            }
        };
        plugin.getRunnables().add(bukkitRunnable);
        bukkitRunnable.runTaskLater(plugin, ticksLater);
    }

    public static void doSyncRepeating(Runnable runnable, long startAfterTicks, long repeatDelayTicks) {
        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable ignored) {
                    plugin.cancelTask(this.getTaskId());
                }
            }
        };
        plugin.getRunnables().add(bukkitRunnable);
        bukkitRunnable.runTaskTimer(plugin, startAfterTicks, repeatDelayTicks);
    }

    public DataStorage getDataStorage() {
        return dataStorage;
    }

    @Override
    public void onEnable() {

        FreeForAll.plugin = this;

        getLogger().info("Performing plugin startup procedure...");
        getLogger().info("Checking for configuration file in plugin data folder...");
        if (!new File(getDataFolder(), "config.yml").exists()) {
            getLogger().info("Failed to find configuration, saving default config...");
            saveDefaultConfig();
        }

        getLogger().info("Validating configuration file...");
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.load(new InputStreamReader(getResource("config.yml"), Charsets.UTF_8));
            syncConfig(config, getConfig());
            saveConfig();
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to validate configuration file.", e);
        }

        getLogger().info("Loading configuration values...");
        configuration = new Configuration(this);
        configuration.load();

        getLogger().info("Checking for available updates...");
        Updater updater = new Updater(this);
        try {
            updater.checkUpdate("v" + getConfiguration().getVersion());

            String latestVersion = updater.getLatestVersion();
            if (latestVersion != null) {
                getLogger().info("A new version of FreeForAll is available for download!");
            } else {
                getLogger().info("FreeForAll is up to date (currently running v" + configuration.getVersion() + ")");
            }

            doAsyncRepeating(()->{

                plugin.getLogger().info("Checking for updates...");

                try {
                    updater.checkUpdate("v"+plugin.getConfiguration().getVersion());
                } catch (IOException ex) {
                    plugin.getLogger().info("An error occurred whilst checking for updates.");
                    plugin.getLogger().info("Message: "+ex.getMessage());
                }

                String latest = updater.getLatestVersion();
                if (latest != null) {
                    plugin.getLogger().info("A new version of FreeForAll is available for download (v"+latestVersion+")!");
                } else {
                    plugin.getLogger().info("FreeForAll is up to date (currently running v" + configuration.getVersion() + ")");
                }

            }, 60 * 60 * 6 * 20, 60 * 60 * 6 * 20);
        } catch (IOException ex) {
            getLogger().info("An error occurred whilst checking for updates.");
            getLogger().info("Message: " + ex.getMessage());
        }

        String storageMethod = configuration.getStorageMethod();
        if (storageMethod.equalsIgnoreCase("yaml") || storageMethod.equalsIgnoreCase("yml")) {
            getLogger().info("This plugin is setup using the YAML storage method, retrieving YAML data file...");
            File file = configuration.getYAMLDataFile();
            dataStorage = new YamlStorage(this, file);
        } else if (storageMethod.equalsIgnoreCase("mysql")) {
            getLogger().info("This plugin is setup using the MYSQL storage method, retrieving MySQL details...");
            String host = configuration.getSQLHost();
            String database = configuration.getSQLDatabase();
            String username = configuration.getSQLUser();
            String password = configuration.getSQLPassword();
            Integer port = configuration.getSQLPort();
            dataStorage = new MySQLStorage(this, host, database, username, password, port);
        } else if (storageMethod.equalsIgnoreCase("sqlite")) {
            getLogger().info("This plugin is setup using the SQLITE storage method (flat file), retrieving SQLITE data file...");
            File file = configuration.getSQLiteDataFile();
            dataStorage = new SQLiteStorage(this, file);
        } else {
            getLogger().info("Unknown storage method \"" + storageMethod + "\".");
            getLogger().info("Valid storage methods include: yaml (or yml), mysql and sqlite");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Initializing plugin data storage...");
        if (!dataStorage.initialize()) {
            String message = "Failed to initialize plugin data storage, please check the logs for further details.";
            getServer().getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(player ->
                    player.sendMessage("[FreeForAll] " + message));
            getPluginLoader().disablePlugin(this);
            return;
        }

        getLogger().info("Registering event listeners...");

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new EntityDamageListener(this), this);
        manager.registerEvents(new PlayerDeathListener(this), this);
        manager.registerEvents(new PlayerLoginListener(this), this);
        manager.registerEvents(new PlayerJoinListener(this), this);
        manager.registerEvents(new PlayerQuitListener(this), this);
        manager.registerEvents(new PlayerRespawnListener(this), this);
        manager.registerEvents(new EnvironmentListeners(this), this);

        getLogger().info("FreeForAll is currently listening to " + HandlerList.getRegisteredListeners(this).size() + " events!");

        getLogger().info("Registering commands...");

        //Might convert to using custom command handler at some point,
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("resetstats").setExecutor(new ResetStatsCommand(this));
        getCommand("kit").setExecutor(new KitCommand(this));
        getCommand("savekit").setExecutor(new SaveKitCommand(this));
        getCommand("delkit").setExecutor(new DelKitCommand(this));
        getCommand("leaderboard").setExecutor(new LeaderboardCommand(this));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("delspawn").setExecutor(new DelSpawnCommand(this));

        getLogger().info("Commands registered!");

        KitManager.loadKits();

        int online = getServer().getOnlinePlayers().size();
        if (online > 0) {
            getLogger().info("Server reload detected, please refrain from doing this in the future as this can " +
                    "massively impair server performance.");
            getLogger().info("Attempting to load user data of " + online + " players, this may take a while...");

            for (Player player : getServer().getOnlinePlayers()) {
                UUID playerUUID = player.getUniqueId();
                Optional<User> optional = dataStorage.loadUser(playerUUID);

                if (!optional.isPresent()) { //Should always be false
                    optional = dataStorage.createUser(playerUUID, player.getName());
                    if (!optional.isPresent()) {
                        getLogger().info("Failed to generate user data for " + player.getName() + "!");
                        player.kickPlayer(ChatColor.RED + "Failed to generate player data, please relog.");
                        continue;
                    }
                }

                User user = optional.get();
                if (!user.getName().equals(player.getName())) {
                    user.setName(player.getName());
                }

                UserManager.getUsers().add(user);
            }

            getLogger().info("Loaded data of " + UserManager.getUsers().size() + "/" + online + " players."); //Incase some failed

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

        }, 60 * 10 * 20, 60 * 10 * 20); //Every 10 minutes repeat this task

        configuration.set("version", getDescription().getVersion());
        configuration.saveConfiguration();

        getLogger().info("Plugin startup procedure complete!");

    }

    @Override
    public void onDisable() {
        getLogger().info("Performing plugin shutdown procedure");
        if (dataStorage != null) dataStorage.close();
        if (configuration != null) configuration.unload();

        if (runnables.size() >  0){//More for aesthetic reasons than anything else
            getLogger().info("Shutting down " + runnables.size() + " tasks...");
            getRunnables().forEach(BukkitRunnable::run);
        }

        //Cancel all tasks
        getServer().getScheduler().cancelTasks(this);

        getLogger().info("Un-registering event listeners...");
        HandlerList.unregisterAll(this);

        FreeForAll.plugin = null;

        getLogger().info("Plugin shutdown procedure complete!");
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void cancelTask(int taskId) {
        getRunnables().stream().filter(runnable -> runnable.getTaskId() == taskId).findFirst()
                .ifPresent(runnable -> {
                    runnable.cancel();
                    runnables.remove(runnable);
                });
    }

    public List<BukkitRunnable> getRunnables() {
        return runnables;
    }

    private void syncConfig(ConfigurationSection from, ConfigurationSection to) {
        from.getKeys(true).stream().filter(fromKey -> !to.contains(fromKey)).forEachOrdered(fromKey -> to.set(fromKey, from.get(fromKey)));
    }
}
