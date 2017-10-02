package me.angrypostman.freeforall.user;

import com.google.common.base.Preconditions;
import me.angrypostman.freeforall.FreeForAll;
import org.bukkit.entity.Player;

import java.util.UUID;

public class User{

    private int playerId;
    private UUID playerUUID;
    private String name;

    private UserData userData;
    private long downloadTime;

    private FreeForAll plugin;

    public User(int playerId, UUID playerUUID, String name){
        this(playerId, playerUUID, name, 0, 0, 0);
    }

    public User(int playerId, UUID playerUUID, String name, int points, int kills, int deaths){
        Preconditions.checkNotNull(playerUUID, "player unique id cannot be null");
        Preconditions.checkArgument(name != null && !name.isEmpty(), "player name cannot be null " +
                "or effectively null");
        this.playerId=playerId;
        this.playerUUID=playerUUID;
        this.name=name;
        this.downloadTime=System.currentTimeMillis();
        this.plugin=FreeForAll.getPlugin();
        this.userData=new UserData(this, kills, deaths, points);
    }

    public int getPlayerId(){
        return playerId;
    }

    public UUID getUniqueId(){
        return playerUUID;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name=name;
    }

    public String getLookupName(){
        return name.toLowerCase();
    }

    public Player getBukkitPlayer(){
        return plugin.getServer().getPlayer(getUniqueId());
    }

    public boolean isOnline(){
        Player player=getBukkitPlayer();
        return player != null && player.isOnline();
    }

    public UserData getUserData(){
        return userData;
    }

    public boolean isSpectating(){
        return UserCache.isSpectating(this);
    }

    public long getDownloadTime(){
        return downloadTime;
    }
}
