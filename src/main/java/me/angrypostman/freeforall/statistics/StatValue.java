package me.angrypostman.freeforall.statistics;

import com.google.common.base.Preconditions;

import me.angrypostman.freeforall.user.User;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class StatValue implements Cloneable {

    private User user;
    private Statistic statistic;
    private int value;

    public StatValue(User user, Statistic statistic, int value) {
        this.user = user;
        this.statistic = statistic;
        this.value = value;
    }

    public User getUser() {
        return user;
    }

    public Statistic getStatistic() {
        return statistic;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        Preconditions.checkArgument(value >= 0, "value cannot be negative");
        this.value = value;
    }

    @Override
    public StatValue clone() {
        try {
            return (StatValue) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new Error(ex);
        }
    }
}
