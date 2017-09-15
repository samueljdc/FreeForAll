package me.angrypostman.freeforall.user;

import com.google.common.base.Preconditions;
import me.angrypostman.freeforall.statistics.StatValue;
import me.angrypostman.freeforall.statistics.Statistic;

public class UserData{

    private User user;
    private StatValue kills;
    private StatValue killStreak;
    private StatValue deaths;
    private StatValue points;
    UserData(User user, int kills, int deaths, int points){
        this.kills=new StatValue(user, Statistic.getStatistic("kills"), kills);
        this.deaths=new StatValue(user, Statistic.getStatistic("deaths"), deaths);
        this.points=new StatValue(user, Statistic.getStatistic("points"), points);
        this.killStreak=new StatValue(user, Statistic.getStatistic("kill_streak"));
    }

    public User getUser(){
        return user;
    }

    public StatValue getKills(){
        return kills;
    }

    public void addKill(){
        kills.setValue(kills.getValue()+1);
        killStreak.setValue(killStreak.getValue()+1);
    }

    public StatValue getDeaths(){
        return deaths;
    }

    public void addDeath(){
        deaths.setValue(deaths.getValue()+1);
    }

    public double getKillDeathRatio(){
        if(getDeaths().getValue() <= 1){
            return getKills().getValue();
        }
        return Double.parseDouble(String.format("%.2f", ((double) getKills().getValue() / getDeaths().getValue())));
    }

    public boolean hasKillStreak(){
        return getKillStreak().getValue() > 1;
    }

    public StatValue getKillStreak(){
        return killStreak;
    }

    public void endStreak(){
        killStreak.setValue(0);
    }

    public StatValue getPoints(){
        return points;
    }

    public void addPoints(int add){
        Preconditions.checkArgument(add > 0, "points to add must be greater than 0");
        points.setValue(points.getValue() + add);
    }

    public void subtractPoints(int subtract){
        Preconditions.checkArgument(subtract >= 0, "points to subtract must be greater than 0");
        Preconditions.checkArgument(this.points.getValue() - subtract >= 0, "cannot reduce a players points below 0");
        points.setValue(points.getValue() - subtract);
    }


    public int getRating(){
        return 0;
    }

    public void resetStats(){
        
        kills.setValue(0);
        deaths.setValue(0);
        killStreak.setValue(0);
        points.setValue(0);
        
    }

}
