package me.angrypostman.freeforall.statistics;

import com.google.common.base.Preconditions;
import me.angrypostman.freeforall.user.User;
import org.bukkit.entity.Player;

import java.util.Optional;

public class Statistic {

    private String name;
    private String friendlyName;
    private String description;

    private int defaultValue;

    public Statistic(String name, String friendlyName) {
        this.name = name;
        this.friendlyName = friendlyName;
    }

    public String getName() {
        return name;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Optional<StatValue> getValue(Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkState(player.isOnline(), "player is not online");
        return StatisticsManager.getValue(player, this);
    }

    public Optional<StatValue> getValue(User user) {
        Preconditions.checkNotNull(user, "user cannot be null");
        return getValue(user.getBukkitPlayer());
    }
}
