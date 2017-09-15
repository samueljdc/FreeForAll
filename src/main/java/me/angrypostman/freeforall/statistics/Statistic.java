package me.angrypostman.freeforall.statistics;

import com.google.common.collect.Maps;

import java.util.Map;

public enum Statistic{

    KILLS("kills", "Kills", "The total kills a player has."),
    DEATHS("deaths", "Deaths", "The total times a player has died."),
    POINTS("points", "Points", "The total points of a player."),
    KILL_STREAK("kill_streak", "Kill Streak", "Total amounts of kills a player has had.");

    private static final Map<String, Statistic> BY_NAME=Maps.newHashMap();

    static{
        for (Statistic stat:values())BY_NAME.put(stat.getName(), stat);
    }

    private String name;
    private String friendlyName;
    private String description;
    private int defaultValue;

    Statistic(String name, String friendlyName, String description){
        this.name=name;
        this.friendlyName=friendlyName;
        this.description=description;
    }

    Statistic(String name, String friendlyName, String description, int defaultValue){
        this.name=name;
        this.friendlyName=friendlyName;
        this.description=description;
        this.defaultValue=defaultValue;
    }

    public String getName(){
        return name;
    }

    public String getFriendlyName(){
        return friendlyName;
    }

    public String getDescription(){
        return description;
    }

    public int getDefaultValue(){
        return defaultValue;
    }

    public static Statistic getStatistic(String name){
        return BY_NAME.get(name);
    }

}
