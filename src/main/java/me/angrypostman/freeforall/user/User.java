package me.angrypostman.freeforall.user;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class User {

    private int playerId = 0;
    private UUID playerUUID = null;
    private String name = null;

    private int points = 0;
    private int kills = 0;
    private int deaths = 0;
    private int killStreak = 0;

    private long downloadTime = 0L;
    private long lastQueried = 0L; //this might be used, i dont know

    public User(int playerId, UUID playerUUID, String name) {
        this(playerId, playerUUID, name, 0, 0, 0);
    }

    public User(int playerId, UUID playerUUID, String name, int points, int kills, int deaths) {
        this.playerId = playerId;
        this.playerUUID = playerUUID;
        this.name = name;
        this.points = points;
        this.kills = kills;
        this.deaths = deaths;
        this.downloadTime = System.currentTimeMillis();
        this.lastQueried = System.currentTimeMillis();
    }

    public int getPlayerId() {
        return playerId;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLookupName() {
        return getName() == null ? null : getName().toLowerCase();
    }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(getPlayerUUID());
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int points) {
        Preconditions.checkArgument(points > 0, "points to add must be greater than 0");
        this.points += points;
    }

    public void subtractPoints(int subtract) {
        Preconditions.checkArgument(subtract > 0, "points to subtract must be greater than 0");
        Preconditions.checkArgument(points - subtract >= 0, "cannot reduce a players points below 0");
        this.points -= subtract;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
        this.killStreak++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        this.deaths++;
    }

    public boolean hasKillStreak() {
        return getKillStreak() > 3;
    }

    public int getKillStreak() {
        return killStreak;
    }

    public void endStreak() {
        this.killStreak = 0;
    }

    public long getDownloadTime() {
        return downloadTime;
    }

    public long getLastQueried() {
        return lastQueried;
    }
}
