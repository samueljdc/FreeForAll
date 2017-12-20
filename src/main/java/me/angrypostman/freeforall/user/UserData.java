package me.angrypostman.freeforall.user;

import com.google.common.base.Preconditions;
import me.angrypostman.freeforall.statistics.StatValue;
import me.angrypostman.freeforall.statistics.Statistic;

public class UserData{

    UserData(final User user,
             final int kills,
             final int deaths,
             final int points){
        this.kills=new StatValue(user, Statistic.getStatistic("kills"), kills);
        this.deaths=new StatValue(user, Statistic.getStatistic("deaths"), deaths);
        this.points=new StatValue(user, Statistic.getStatistic("points"), points);
        this.killStreak=new StatValue(user, Statistic.getStatistic("kill_streak"));
    }

    public User getUser(){
        return this.user;
    }

    public void addKill(){
        this.kills.setValue(this.kills.getValue()+1);
        this.killStreak.setValue(this.killStreak.getValue()+1);
    }

    public void addDeath(){
        this.deaths.setValue(this.deaths.getValue()+1);
    }

    public double getKillDeathRatio(){
        if(getDeaths().getValue()<=1){ //avoid divide by 0 errors and no point in dividing by 1
            return getKills().getValue();
        }
        return Double.parseDouble(String.format("%.2f", ((double) getKills().getValue()/getDeaths().getValue())));
    }

    public StatValue getKills(){
        return this.kills.clone();
    }

    public StatValue getDeaths(){
        return this.deaths.clone();
    }

    public boolean hasKillStreak(){
        return getKillStreak().getValue()>1;
    }

    public StatValue getKillStreak(){
        return this.killStreak;
    }

    public void endStreak(){
        this.killStreak.setValue(0);
    }

    public StatValue getPoints(){
        return this.points.clone();
    }

    public void addPoints(final int add){
        Preconditions.checkArgument(add>0, "points to add must be greater than 0");
        this.points.setValue(this.points.getValue()+add);
    }

    public void subtractPoints(final int subtract){
        Preconditions.checkArgument(subtract>=0, "points to subtract must be greater than 0");
        Preconditions.checkArgument(this.points.getValue()-subtract>=0, "cannot reduce a players points below 0");
        this.points.setValue(this.points.getValue()-subtract);
    }

    public StatValue getRank(){
        return new StatValue(null, null);
    }

    public void resetStats(){
        this.kills.setValue(0);
        this.deaths.setValue(0);
        this.killStreak.setValue(0);
        this.points.setValue(0);
    }

    private User user;
    private final StatValue kills;
    private final StatValue killStreak;
    private final StatValue deaths;
    private final StatValue points;
}
