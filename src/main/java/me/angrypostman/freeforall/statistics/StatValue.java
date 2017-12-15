package me.angrypostman.freeforall.statistics;

import com.google.common.base.Preconditions;
import me.angrypostman.freeforall.user.User;

public class StatValue implements Cloneable{

    private User user;
    private Statistic parent;

    private int value;

    public StatValue(User user, Statistic parent){
        this(user, parent, parent == null ? 0 : parent.getDefaultValue());
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
        Preconditions.checkArgument(value >= 0, "value cannot be less than 0");
        this.value=value;
    }

    @Override
    public StatValue clone(){
        try{
            return (StatValue)super.clone();
        } catch(CloneNotSupportedException e){
            e.printStackTrace();
        }
        return null;
    }
}
