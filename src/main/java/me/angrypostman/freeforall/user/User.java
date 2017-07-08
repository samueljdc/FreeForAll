package me.angrypostman.freeforall.user;

import me.angrypostman.freeforall.FreeForAll;
import org.bukkit.entity.Player;

import java.util.UUID;

public class User {

    private int playerId = 0;
    private UUID playerUUID = null;
    private String name = null;

    private UserData userData;
    private long downloadTime = 0L;

    private FreeForAll plugin = null;

    public User(int playerId, UUID playerUUID, String name) {
        this(playerId, playerUUID, name, 0, 0, 0);
    }

    public User(int playerId, UUID playerUUID, String name, int points, int kills, int deaths) {
        this.playerId = playerId;
        this.playerUUID = playerUUID;
        this.name = name;
        this.userData = new UserData(kills, deaths, points/*, Maps.newHashMap()*/);
        this.downloadTime = System.currentTimeMillis();
        this.plugin = FreeForAll.getPlugin();
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
        return plugin.getServer().getPlayer(getPlayerUUID());
    }

    public UserData getUserData() {
        return userData;
    }

    public long getDownloadTime() {
        return downloadTime;
    }
}
