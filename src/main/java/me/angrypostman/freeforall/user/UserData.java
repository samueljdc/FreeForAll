package me.angrypostman.freeforall.user;

import com.google.common.base.Preconditions;
import me.angrypostman.freeforall.statistics.StatValue;
import me.angrypostman.freeforall.statistics.Statistic;

import java.util.Map;

public class UserData {

    private int kills;
    private int deaths;
    private int points;
    private int killStreak;
    private Map<Statistic, StatValue> stats;

    UserData(int kills, int deaths, int points/*, Map<Statistic, StatValue> stats*/) {
        this.kills = kills;
        this.deaths = deaths;
        this.points = points;
        //this.stats = stats;
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


    public double getKillDeathRatio() {
        if (getDeaths() <= 1) {
            return getKills();
        }
        return Double.parseDouble(String.format("%.2f", ((double) getKills() / getDeaths())));
    }


    public boolean hasKillStreak() {
        return getKillStreak() > 0;
    }


    public int getKillStreak() {
        return killStreak;
    }


    public void endStreak() {
        this.killStreak = 0;
    }


    public int getPoints() {
        return points;
    }


//    public Map<Statistic, StatValue> getStatistics() {
//        return null;
//    }


    public void addPoints(int points) {
        Preconditions.checkArgument(points > 0, "points to add must be greater than 0");
        this.points += points;
    }


    public void subtractPoints(int subtract) {
        Preconditions.checkArgument(subtract > 0, "points to subtract must be greater than 0");
        Preconditions.checkArgument(points - subtract >= 0, "cannot reduce a players points below 0");
        this.points -= subtract;
    }


    public void resetStats() {

        this.kills = 0;
        this.deaths = 0;
        this.points = 0;
        this.killStreak = 0;

    }

}
