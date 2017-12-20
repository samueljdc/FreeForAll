package me.angrypostman.freeforall.user;

import com.google.common.base.Preconditions;
import java.util.UUID;
import me.angrypostman.freeforall.FreeForAll;
import org.bukkit.entity.Player;

public class User{

    private static final FreeForAll plugin=FreeForAll.getPlugin();

    private final int playerId;
    private final UUID playerUUID;
    private final UserData userData;
    private final long downloadTime;

    private String name;
    private Damage lastDamage;

    public User(final int playerId,
                final UUID playerUUID,
                final String name){
        this(playerId, playerUUID, name, 0, 0, 0);
    }

    public User(final int playerId,
                final UUID playerUUID,
                final String name,
                final int points,
                final int kills,
                final int deaths){
        Preconditions.checkNotNull(playerUUID, "player unique id cannot be null");
        Preconditions.checkArgument(name!=null&&!name.isEmpty(), "player name cannot be null "+"or effectively null");
        this.playerId=playerId;
        this.playerUUID=playerUUID;
        this.name=name;
        this.downloadTime=System.currentTimeMillis();
        this.userData=new UserData(this, kills, deaths, points);
    }

    public int getPlayerId(){
        return this.playerId;
    }

    public String getName(){
        return this.name;
    }

    public void setName(final String name){
        this.name=name;
    }

    public String getLookupName(){
        return this.name.toLowerCase();
    }

    public boolean isOnline(){
        final Player player=getBukkitPlayer();
        return player!=null&&player.isOnline();
    }

    public Player getBukkitPlayer(){
        return User.plugin.getServer()
                          .getPlayer(getUniqueId());
    }

    public UUID getUniqueId(){
        return this.playerUUID;
    }

    public UserData getUserData(){
        return this.userData;
    }

    public boolean isSpectating(){
        return UserCache.isSpectating(this);
    }

    public Damage getLastDamage(){
        return this.lastDamage;
    }

    public void setLastDamage(final Damage lastDamage){
        this.lastDamage=lastDamage;
    }

    public long getDownloadTime(){
        return this.downloadTime;
    }
}
