package me.angrypostman.freeforall;

import com.google.common.base.Charsets;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
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

public class FreeForAll extends JavaPlugin{

    private static FreeForAll plugin;
    private DataStorage dataStorage=null;
    private Configuration configuration=null;
    private Updater updater=null;
    private boolean isVault;
    private Chat chat;

    //We want every Runnable that we schedule to be logged
    //So we can later forcibly run them on shutdown
    private final List<BukkitRunnable> runnables=new CopyOnWriteArrayList<>();

    public static void doAsync(final Runnable runnable){
        final BukkitRunnable bukkitRunnable=new BukkitRunnable(){
            @Override
            public void run(){
                try{
                    runnable.run();
                }catch(final Throwable ex){
                    plugin.getLogger()
                          .info("Task #"+getTaskId()+" generated an uncaught exception");
                    ex.printStackTrace();
                }
            }
        };
        bukkitRunnable.runTaskAsynchronously(plugin);
    }

    public static void doAsyncLater(final Runnable runnable,
                                    final long ticksLater){
        final BukkitRunnable bukkitRunnable=new BukkitRunnable(){
            @Override
            public void run(){
                try{
                    runnable.run();
                }catch(final Throwable ex){
                    plugin.getLogger()
                          .info("Task #"+getTaskId()+" generated an uncaught exception");
                    ex.printStackTrace();
                }
                //Make sure that the task gets cancelled no matter what
                plugin.cancelTask(this.getTaskId());
            }
        };
        FreeForAll.getPlugin().runnables.add(bukkitRunnable);
        bukkitRunnable.runTaskLaterAsynchronously(plugin, ticksLater);
    }

    public static FreeForAll getPlugin(){
        return plugin;
    }

    public void cancelTask(final int taskId){
        this.runnables.removeIf(runnable->runnable.getTaskId()==taskId);
    }

    public static void doSync(final Runnable runnable){
        final BukkitRunnable bukkitRunnable=new BukkitRunnable(){
            @Override
            public void run(){
                try{
                    runnable.run();
                }catch(final Throwable ex){
                    plugin.getLogger()
                          .info("Task #"+getTaskId()+" generated an uncaught exception");
                    ex.printStackTrace();
                }
            }
        };
        bukkitRunnable.runTask(plugin);
    }

    public static void doSyncLater(final Runnable runnable,
                                   final long ticksLater){
        final BukkitRunnable bukkitRunnable=new BukkitRunnable(){
            @Override
            public void run(){
                try{
                    runnable.run();
                }catch(final Throwable ex){
                    plugin.getLogger()
                          .info("Task #"+getTaskId()+" generated an uncaught exception");
                    ex.printStackTrace();
                }
                //Make sure that the task gets cancelled no matter what
                plugin.cancelTask(this.getTaskId());
            }
        };
        FreeForAll.getPlugin().runnables.add(bukkitRunnable);
        bukkitRunnable.runTaskLater(plugin, ticksLater);
    }

    public DataStorage getDataStorage(){
        return this.dataStorage;
    }

    @Override
    public void onDisable(){
        getLogger().info("Performing plugin shutdown procedure...");

        if(this.dataStorage!=null){ this.dataStorage.close(); }
        if(this.configuration!=null){ this.configuration.unload(); }

        this.runnables.forEach(runnable->{

        });

        //Cancel all tasks
        getServer().getScheduler()
                   .cancelTasks(this);

        getLogger().info("Un-registering event listeners...");
        HandlerList.unregisterAll(this);

        this.runnables.clear();

        this.chat=null;
        this.updater=null;
        this.configuration=null;
        this.dataStorage=null;
        this.isVault=false;

        FreeForAll.plugin=null;

        getLogger().info("Plugin shutdown procedure complete!");
    }

    @Override
    public void onEnable(){

        FreeForAll.plugin=this;

        getLogger().info("Performing plugin startup procedure...");

        getLogger().info("Checking for configuration file in plugin data folder...");
        if(!new File(getDataFolder(), "config.yml").exists()){
            getLogger().info("Failed to find configuration, saving default config...");
            saveDefaultConfig();
        }

        getLogger().info("Validating configuration file...");
        try{
            final YamlConfiguration config=new YamlConfiguration();
            config.load(new BufferedReader(new InputStreamReader(getResource("config.yml"), Charsets.UTF_8)));
            syncConfig(config, getConfig());
            saveConfig();
        }catch(final Exception e){
            getLogger().log(Level.WARNING, "Failed to validate configuration file.", e);
        }

        getLogger().info("Loading configuration values...");
        this.configuration=new Configuration(this);
        this.configuration.load();

        Message.load(getConfig());

        this.isVault=this.configuration.isChatFormatting()&&getServer().getPluginManager()
                                                                       .getPlugin("Vault")!=null;
        if(this.isVault){
            final RegisteredServiceProvider<Chat> provider=getServer().getServicesManager()
                                                                      .getRegistration(Chat.class);
            if(provider.getProvider()==null){
                getLogger().info("Vault was found but failed to get service provider for "+Chat.class);
                this.isVault=false;
            }else{
                this.chat=provider.getProvider();
                getLogger().info("Successfully hooked into Vault");
            }
        }

        final String storageMethod=this.configuration.getStorageMethod();
        if(storageMethod.equalsIgnoreCase("mysql")){
            getLogger().info("This plugin is setup using the MYSQL storage method, retrieving MySQL details...");
            final String host=this.configuration.getSQLHost();
            final String database=this.configuration.getSQLDatabase();
            final String username=this.configuration.getSQLUser();
            final String password=this.configuration.getSQLPassword();
            final Integer port=this.configuration.getSQLPort();

            if(host.isEmpty()||database.isEmpty()||username.isEmpty()||port==0){
                getLogger().info("MySQL details improperly configured.");
                getPluginLoader().disablePlugin(this);
                return;
            }else if(username.equals("root")){ //So many people use "root" for accessing their database... plsno
                getLogger().info("Please refrain from using the root account to access your databases.");
            }

            this.dataStorage=new MySQLStorage(this, host, database, username, password, port);
        }else if(storageMethod.equalsIgnoreCase("sqlite")){
            getLogger().info(
                    "This plugin is setup using the SQLITE storage method (flat file), retrieving SQLITE data file...");
            final File file=this.configuration.getSQLiteDataFile();
            this.dataStorage=new SQLiteStorage(this, file);
        }else{
            getLogger().info("Unknown storage method \""+storageMethod+"\".");
            getLogger().info("Valid storage methods include: mysql and sqlite");
            getServer().getPluginManager()
                       .disablePlugin(this);
            return;
        }

        getLogger().info("Initializing plugin data storage...");
        if(!this.dataStorage.initialize()){
            getLogger().info("Failed to initialize plugin data storage, please check the logs for further details.");
            getPluginLoader().disablePlugin(this);
            return;
        }

        getLogger().info("Loading kits...");
        KitManager.loadKits();
        getLogger().info("Loaded "+KitManager.getKits()
                                             .size()+" kits!");

        final int online=getServer().getOnlinePlayers()
                                    .size();
        if(online>0){
            getLogger().info(
                    "Server reload detected, please refrain from doing this in the future as this can "+"massively impair server performance.");
            getLogger().info("Attempting to load user data of "+online+" players, this may take a while...");

            for(final Player player : getServer().getOnlinePlayers()){
                final UUID uniqueId=player.getUniqueId();
                Optional<User> optional=this.dataStorage.loadUser(uniqueId);

                if(!optional.isPresent()){ //Should always be false
                    optional=this.dataStorage.createUser(uniqueId, player.getName());
                    if(!optional.isPresent()){
                        getLogger().info("Failed to generate user data for '"+player.getName()+"'!");
                        player.kickPlayer(ChatColor.RED+"Failed to generate player data, please relog.");
                        continue;
                    }
                }

                final User user=optional.get();
                if(!user.getName()
                        .equals(player.getName())){
                    user.setName(player.getName());
                }

                UserCache.cacheUser(user);
            }

            //In the event that some peoples player data failed to load we only show the amount
            //that actually got loaded
            getLogger().info("Loaded data of "+UserCache.getUsers()
                                                        .size()+"/"+online+" players.");
        }

        getLogger().info("Registering event listeners...");

        final PluginManager manager=getServer().getPluginManager();
        manager.registerEvents(new EntityDamageListener(this), this);
        manager.registerEvents(new PlayerDeathListener(this), this);
        manager.registerEvents(new PlayerLoginListener(this), this);
        manager.registerEvents(new PlayerJoinListener(this), this);
        manager.registerEvents(new PlayerQuitListener(this), this);
        manager.registerEvents(new PlayerRespawnListener(this), this);
        manager.registerEvents(new PlayerListeners(this), this);
        manager.registerEvents(new PlayerChatListener(this), this);

        getLogger().info("Event listeners registered!");

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
        getCommand("spectate").setExecutor(new SpectateCommand(this));

        getLogger().info("Commands registered!");

        getLogger().info("Starting cache cleaner...");
        doSyncRepeating(()->{

            int cleanupCount=0;

            for(final User user : UserCache.getUsers()){
                final Player player=user.getBukkitPlayer();
                final long downloadTime=user.getDownloadTime();
                if(player==null||!player.isOnline()){
                    if(System.currentTimeMillis()-downloadTime>=TimeUnit.MINUTES.toMillis(10)){
                        UserCache.expireUser(user);
                        cleanupCount++;
                    }
                }
            }

            if(cleanupCount>0){
                getLogger().info("Cleaned up "+cleanupCount+" users from memory.");
            }
        }, 60*10*20, 60*10*20); //Every 10 minutes repeat this task

        getLogger().info("Updating saved version string in config to current version...");
        this.configuration.set("version", getDescription().getVersion());
        this.configuration.saveConfiguration();

        getLogger().info("Plugin startup procedure complete!");
        this.updater=new Updater(this);
        doAsyncRepeating(()->{

            plugin.getLogger()
                  .info("Checking for available updates...");
            try{
                final PluginDescriptionFile description=plugin.getDescription();
                this.updater.checkUpdate(description.getVersion());
            }catch(final IOException ex){
                plugin.getLogger()
                      .info("An error occurred whilst checking for updates.");
                plugin.getLogger()
                      .info("Message: "+ex.getMessage());
            }

            final String latestVersion=this.updater.getLatestVersion();
            final String latestVersionURL=this.updater.getLatestVersionURL();
            if(latestVersion!=null){
                plugin.getLogger()
                      .info("A new version of FreeForAll is available for download (v"+latestVersion+")!");
                plugin.getLogger()
                      .info("Download it now at: "+latestVersionURL);
            }else{
                plugin.getLogger()
                      .info("FreeForAll is currently up to date (running v"+this.configuration.getVersion()+")");
            }
        }, 0L, 60*60*6*20);
    }

    public static void doAsyncRepeating(final Runnable runnable,
                                        final long startAfterTicks,
                                        final long repeatDelayTicks){
        final BukkitRunnable bukkitRunnable=new BukkitRunnable(){
            @Override
            public void run(){
                try{
                    runnable.run();
                }catch(final Throwable ex){
                    plugin.getLogger()
                          .info("Task #"+getTaskId()+" generated an uncaught exception");
                    plugin.cancelTask(this.getTaskId());
                    ex.printStackTrace();
                }
            }
        };
        FreeForAll.getPlugin().runnables.add(bukkitRunnable);
        bukkitRunnable.runTaskTimerAsynchronously(plugin, startAfterTicks, repeatDelayTicks);
    }

    public static void doSyncRepeating(final Runnable runnable,
                                       final long startAfterTicks,
                                       final long repeatDelayTicks){
        final BukkitRunnable bukkitRunnable=new BukkitRunnable(){
            @Override
            public void run(){
                try{
                    runnable.run();
                }catch(final Throwable ex){
                    plugin.getLogger()
                          .info("Task #"+getTaskId()+" generated an uncaught exception");
                    plugin.cancelTask(this.getTaskId());
                    ex.printStackTrace();
                }
            }
        };
        FreeForAll.getPlugin().runnables.add(bukkitRunnable);
        bukkitRunnable.runTaskTimer(plugin, startAfterTicks, repeatDelayTicks);
    }

    private void syncConfig(final ConfigurationSection from,
                            final ConfigurationSection to){
        from.getKeys(true)
            .stream()
            .filter(fromKey->!to.isSet(fromKey))
            .forEachOrdered(fromKey->to.set(fromKey, from.get(fromKey)));
    }

    public String getVersion(){
        return this.configuration.getVersion();
    }

    public Configuration getConfiguration(){
        return this.configuration;
    }

    public Updater getUpdater(){
        return this.updater;
    }

    public boolean hasVault(){
        return this.isVault;
    }

    public Chat getChatManager(){
        return this.chat;
    }
}