package me.angrypostman.freeforall.statistics;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Statistic{

    private String name;
    private String friendlyName;
    private String description;
    private int defaultValue;

    private static final Map<String, Statistic> BY_NAME=Maps.newHashMap();
    private static final Set<Statistic> STATISTICS=Sets.newHashSet();

    public Statistic(String name, String friendlyName){
        this.name=name;
        this.friendlyName=friendlyName;
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

    public void setDescription(String description){
        this.description=description;
    }

    public int getDefaultValue(){
        return defaultValue;
    }

    public void setDefaultValue(int defaultValue){
        this.defaultValue=defaultValue;
    }

    public static Statistic getStatistic(String name){
        return BY_NAME.get(name);
    }

    public static void registerStatistic(Statistic statistic){
        Preconditions.checkNotNull(statistic, "statistic cannot be null");
        Preconditions.checkArgument(BY_NAME.get(statistic.getName())==null, "statistic already defined");
        STATISTICS.add(statistic);
        BY_NAME.put(statistic.getName().toLowerCase().replace(" ", "_"), statistic);
    }

    public static void unregisterAll(){
        STATISTICS.clear();
        BY_NAME.clear();
    }

    public static Collection<Statistic> getStatistics(){
        return Sets.newHashSet(STATISTICS);
    }

}
