package me.angrypostman.freeforall.statistics;

import com.google.common.collect.Maps;
import java.util.Locale;
import java.util.Map;

public enum Statistic{

    KILLS("kills", "Kills", "The total kills a player has."),
    DEATHS("deaths", "Deaths", "The total times a player has died."),
    POINTS("points", "Points", "The total points of a player."),
    KILL_STREAK("kill_streak", "Kill Streak", "Total amounts of kills a player has had.");
    //ALL_TIME_KILL_STREAK("all_time_kill_streak", "Highest Kill Streak", "Highest kill streak this player has ever reached.");

    private static final Map<String, Statistic> BY_NAME=Maps.newHashMap();

    static{
        for(final Statistic stat : values()){
            BY_NAME.put(stat.getName()
                            .toLowerCase(Locale.ENGLISH), stat);
        }
    }

    Statistic(final String name,
              final String friendlyName,
              final String description){
        this.name=name;
        this.friendlyName=friendlyName;
        this.description=description;
    }

    Statistic(final String name,
              final String friendlyName,
              final String description,
              final int defaultValue){
        this.name=name;
        this.friendlyName=friendlyName;
        this.description=description;
        this.defaultValue=defaultValue;
    }

    public static Statistic getStatistic(final String name){
        return BY_NAME.get(name.toLowerCase(Locale.ENGLISH));
    }

    public String getName(){
        return this.name;
    }

    public String getFriendlyName(){
        return this.friendlyName;
    }

    public String getDescription(){
        return this.description;
    }

    public int getDefaultValue(){
        return this.defaultValue;
    }

    private final String name;
    private final String friendlyName;
    private final String description;
    private int defaultValue;

}
