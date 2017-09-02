package me.angrypostman.freeforall.statistics;

import me.angrypostman.freeforall.user.User;

public class StatValue{

    private User user;
    private Statistic parent;
    private int value;

    public StatValue(User user, Statistic parent){
        this(user, parent, parent.getDefaultValue());
    }

    public StatValue(User user, Statistic parent, int value){
        this.user=user;
        this.parent=parent;
        this.value=value;
    }

    public User getUser(){
        return user;
    }

    public Statistic getParent(){
        return parent;
    }

    public int getValue(){
        return value;
    }

    public void setValue(int value){
        this.value=value;
    }
}
