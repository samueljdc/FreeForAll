package me.angrypostman.freeforall;

import com.google.common.base.Charsets;
import me.angrypostman.freeforall.commands.*;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.data.MySQLStorage;
import me.angrypostman.freeforall.data.SQLiteStorage;
import me.angrypostman.freeforall.kit.KitManager;
import me.angrypostman.freeforall.listeners.*;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserCache;
import me.angrypostman.freeforall.util.Configuration;
import me.angrypostman.freeforall.util.Message;
import me.angrypostman.freeforall.util.Updater;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class FreeForAll extends JavaPlugin{

    private static FreeForAll plugin;

    private DataStorage dataStorage=null;
    private Configuration configuration=null;
    private Updater updater=null;
    private boolean isVault;
    private Chat chat;
    private Logger pluginLogger;

    public FreeForAll(){
        this.pluginLogger=new Logger(this);
    }

    //We want every Runnable that we schedule to be logged
    //So we can later forcibly run them on shutdown
    private List<BukkitRunnable> runnables=new CopyOnWriteArrayList<>();

    public static FreeForAll getPlugin(){
        return plugin;
    }

    public static void doAsync(Runnable runnable){
        BukkitRunnable bukkitRunnable=new BukkitRunnable(){
            @Override
            public void run(){
                try{
                    runnable.run();
                } catch(Throwable ex){
                    plugin.getPluginLogger().info("Task #" + getTaskId() + " generated an uncaught exception");
                    ex.getCause().printStackTrace();
                }
            }
        };
        bukkitRunnable.runTaskAsynchronously(plugin);
    }

    public static void doAsyncLater(Runnable runnable, long ticksLater){
        BukkitRunnable bukkitRunnable=new BukkitRunnable(){
            @Override
            public void run(){
                try{
                    runnable.run();
                } catch(Throwable ex){
                    plugin.getPluginLogger().info("Task #" + getTaskId() + " generated an uncaught exception");
                    ex.getCause().printStackTrace();
                }
                //Make sure that the task gets cancelled no matter what
                plugin.cancelTask(this.getTaskId());
            }
        };
        bukkitRunnable.runTaskLaterAsynchronously(plugin, ticksLater);
    }

    public static void doAsyncRepeating(Runnable runnable, long startAfterTicks, long repeatDelayTicks){
        BukkitRunnable bukkitRunnable=new BukkitRunnable(){
            @Override
            public void run(){
                try{
                    runnable.run();
                } catch(Throwable ex){
                    plugin.getPluginLogger().info("Task #" + getTaskId() + " generated an uncaught exception");
                    plugin.cancelTask(this.getTaskId());
                    ex.getCause().printStackTrace();
                }
            }
        };
        plugin.getRunnables().add(bukkitRunnable);
        bukkitRunnable.runTaskTimerAsynchronously(plugin, startAfterTicks, repeatDelayTicks);
    }

    public static void doSync(Runnable runnable){
        BukkitRunnable bukkitRunnable=new BukkitRunnable(){
            @Override
            public void run(){
                try{
                    runnable.run();
                } catch(Throwable ex){
                    plugin.getPluginLogger().info("Task #" + getTaskId() + " generated an uncaught exception");
                    ex.getCause().printStackTrace();
                }
            }
        };
        bukkitRunnable.runTask(plugin);
    }

    public static void doSyncLater(Runnable runnable, long ticksLater){
        BukkitRunnable bukkitRunnable=new BukkitRunnable(){
            @Override
            public void run(){
                try{
                    runnable.run();
                } catch(Throwable ex){
                    plugin.getPluginLogger().info("Task #" + getTaskId() + " generated an uncaught exception");
                    ex.getCause().printStackTrace();
                }
                //Make sure that the task gets cancelled no matter what
                plugin.cancelTask(this.getTaskId());
            }
        };
        plugin.getRunnables().add(bukkitRunnable);
        bukkitRunnable.runTaskLater(plugin, ticksLater);
    }

    public static void doSyncRepeating(Runnable runnable, long startAfterTicks, long repeatDelayTicks){
        BukkitRunnable bukkitRunnable=new BukkitRunnable(){
            @Override
            public void run(){
                try{
                    runnable.run();
                } catch(Throwable ex){
                    plugin.getPluginLogger().info("Task #" + getTaskId() + " generated an uncaught exception");
                    plugin.cancelTask(this.getTaskId());
                    ex.getCause().printStackTrace();
                }
            }
        };
        plugin.getRunnables().add(bukkitRunnable);
        bukkitRunnable.runTaskTimer(plugin, startAfterTicks, repeatDelayTicks);
    }

    public DataStorage getDataStorage(){
        return dataStorage;
    }

    @Override
    public void onEnable(){

        FreeForAll.plugin=this;

        getPluginLogger().info("Performing plugin startup procedure...");

        getPluginLogger().info("Checking for configuration file in plugin data folder...");
        if(!new File(getDataFolder(), "config.yml").exists()){
            getPluginLogger().info("Failed to find configuration, saving default config...");
            saveDefaultConfig();
        }

        getPluginLogger().info("Validating configuration file...");
        try{
            YamlConfiguration config=new YamlConfiguration();
            config.load(new BufferedReader(new InputStreamReader(getResource("config.yml"), Charsets.UTF_8)));
            syncConfig(config, getConfig());
            saveConfig();
        } catch(Exception e){
            getPluginLogger().log(Level.WARNING, "Failed to validate configuration file.", e);
        }

        getPluginLogger().info("Loading configuration values...");
        configuration=new Configuration(this);
        configuration.load();

        Message.load(getConfig());

        isVault=configuration.isChatFormatting() && getServer().getPluginManager().getPlugin("Vault") != null;
        if(isVault){
            RegisteredServiceProvider<Chat> provider=getServer().getServicesManager().getRegistration(Chat.class);
            if(provider.getProvider() == null){
                getPluginLogger().info("Vault was found but failed to get service provider for " + Chat.class);
                isVault=false;
            } else{
                chat=provider.getProvider();
                getPluginLogger().info("Successfully hooked into Vault");
            }
        } else{
            getPluginLogger().info(!configuration.isChatFormatting() ? "Chat formatting is disabled in config.yml, " + "set chatFormatting: true in config.yml to enable this feature" : "Chat formatting is enabled but Vault was not found, chat formatting will not work without Vault installed.");
        }

        String storageMethod=configuration.getStorageMethod();
        if(storageMethod.equalsIgnoreCase("mysql")){
            getPluginLogger().info("This plugin is setup using the MYSQL storage method, retrieving MySQL details...");
            String host=configuration.getSQLHost();
            String database=configuration.getSQLDatabase();
            String username=configuration.getSQLUser();
            String password=configuration.getSQLPassword();
            Integer port=configuration.getSQLPort();
            dataStorage=new MySQLStorage(this, host, database, username, password, port);
        } else if(storageMethod.equalsIgnoreCase("sqlite")){
            getPluginLogger().info("This plugin is setup using the SQLITE storage method (flat file), retrieving SQLITE data file...");
            File file=configuration.getSQLiteDataFile();
            dataStorage=new SQLiteStorage(this, file);
        } else{
            getPluginLogger().info("Unknown storage method \"" + storageMethod + "\".");
            getPluginLogger().info("Valid storage methods include: mysql and sqlite");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getPluginLogger().info("Initializing plugin data storage...");
        if(!dataStorage.initialize()){
            getPluginLogger().info("Failed to initialize plugin data storage, please check the logs for further details.");
            getPluginLoader().disablePlugin(this);
            return;
        }

        KitManager.loadKits();

        int online=getServer().getOnlinePlayers().size();
        if(online > 0){
            getPluginLogger().info("Server reload detected, please refrain from doing this in the future as this can " + "massively impair server performance.");
            getPluginLogger().info("Attempting to load user data of " + online + " players, this may take a while...");

            for(Player player : getServer().getOnlinePlayers()){
                UUID uniqueId=player.getUniqueId();
                Optional<User> optional=dataStorage.loadUser(uniqueId);

                if(!optional.isPresent()){ //Should always be false
                    optional=dataStorage.createUser(uniqueId, player.getName());
                    if(!optional.isPresent()){
                        getPluginLogger().info("Failed to generate user data for '" + player.getName() + "'!");
                        player.kickPlayer(ChatColor.RED + "Failed to generate player data, please relog.");
                        continue;
                    }
                }

                User user=optional.get();
                if(!user.getName().equals(player.getName())){
                    user.setName(player.getName());
                }

                UserCache.cacheUser(user);
            }

            //In the event that some peoples player data failed to load we only show the amount
            //that actually got loaded
            getPluginLogger().info("Loaded data of " + UserCache.getUsers().size() + "/" + online + " players.");

        }

        getPluginLogger().info("Registering event listeners...");

        PluginManager manager=getServer().getPluginManager();
        manager.registerEvents(new EntityDamageListener(this), this);
        manager.registerEvents(new PlayerDeathListener(this), this);
        manager.registerEvents(new PlayerLoginListener(this), this);
        manager.registerEvents(new PlayerJoinListener(this), this);
        manager.registerEvents(new PlayerQuitListener(this), this);
        manager.registerEvents(new PlayerRespawnListener(this), this);
        manager.registerEvents(new PlayerListeners(this), this);
        manager.registerEvents(new PlayerChatListener(this), this);

        getPluginLogger().info("Event listeners registered!");

        getPluginLogger().info("Registering commands...");

        //Might convert to using custom command handler at some point,
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("resetstats").setExecutor(new ResetStatsCommand(this));
        getCommand("kit").setExecutor(new KitCommand(this));
        getCommand("savekit").setExecutor(new SaveKitCommand(this));
        getCommand("delkit").setExecutor(new DelKitCommand(this));
        getCommand("leaderboard").setExecutor(new LeaderboardCommand(this));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("delspawn").setExecutor(new DelSpawnCommand(this));
        getCommand("spectate").setExecutor(new SpectateCommand(this));

        getPluginLogger().info("Commands registered!");

        doSyncRepeating(() -> {

            //Cleanup unused data
            for(User user : UserCache.getUsers()){
                Player player=user.getBukkitPlayer();
                long downloadTime=user.getDownloadTime();
                if(player == null || !player.isOnline()){
                    if(System.currentTimeMillis() - downloadTime >= TimeUnit.MINUTES.toMillis(10)){
                        UserCache.expireUser(user);
                    }
                }
            }

        }, 60 * 10 * 20, 60 * 10 * 20); //Every 10 minutes repeat this task

        configuration.set("version", getDescription().getVersion());
        configuration.saveConfiguration();

        getPluginLogger().info("Plugin startup procedure complete!");
        updater=new Updater(this);
        doAsyncRepeating(() -> {

            plugin.getPluginLogger().info("Checking for available updates...");
            try{
                PluginDescriptionFile description=plugin.getDescription();
                updater.checkUpdate(description.getVersion());
            } catch(IOException ex){
                throw new Error(ex);
            }

            String latestVersion=updater.getLatestVersion();
            String latestVersionURL=updater.getLatestVersionURL();
            if(latestVersion != null){
                plugin.getPluginLogger().info("A new version of FreeForAll is available for download (v" + latestVersion + ")!");
                plugin.getPluginLogger().info("Download it now at: " + latestVersionURL);
            } else{
                plugin.getPluginLogger().info("FreeForAll is currently up to date (running v" + configuration.getVersion() + ")");
            }
        }, 0L, 60 * 60 * 6 * 20);

    }

    @Override
    public void onDisable(){
        getPluginLogger().info("Performing plugin shutdown procedure...");
        if(this.dataStorage != null) this.dataStorage.close();
        if(this.configuration != null) this.configuration.unload();

        if(this.runnables.size() > 0){ //Force all remaining tasks to run
            getPluginLogger().info("Shutting down " + runnables.size() + " tasks...");
            getRunnables().forEach(runnable -> cancelTask(runnable.getTaskId(), true));
        }

        //Cancel all tasks
        getServer().getScheduler().cancelTasks(this);

        getPluginLogger().info("Un-registering event listeners...");
        HandlerList.unregisterAll(this);
        getPluginLogger().info("Plugin shutdown procedure complete!");

        try{
            this.pluginLogger.getOutputStream().flush();
        } catch(IOException ignored){}

        this.pluginLogger=null;
        FreeForAll.plugin=null;
    }

    public Logger getPluginLogger(){
        return this.pluginLogger;
    }

    public String getVersion(){
        return configuration.getVersion();
    }

    public Configuration getConfiguration(){
        return configuration;
    }

    public void cancelTask(int taskId){
        cancelTask(taskId, false);
    }

    public void cancelTask(int taskId, boolean shouldComplete){
        runnables.stream().filter(runnable -> runnable.getTaskId() == taskId).findFirst().ifPresent(runnable -> {
            if(shouldComplete) runnable.run();
            runnable.cancel();
            runnables.remove(runnable);
        });
    }

    public List<BukkitRunnable> getRunnables(){
        return new ArrayList<>(runnables);
    }

    public Updater getUpdater(){
        return updater;
    }

    public boolean hasVault(){
        return isVault;
    }

    public Chat getChat(){
        return chat;
    }

    private void syncConfig(ConfigurationSection from, ConfigurationSection to){
        from.getKeys(true).stream().filter(fromKey -> !to.isSet(fromKey)).forEachOrdered(fromKey -> to.set(fromKey, from.get(fromKey)));
    }
}
