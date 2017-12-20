package me.angrypostman.freeforall.statistics;

import com.google.common.base.Preconditions;
import me.angrypostman.freeforall.user.User;

public class StatValue implements Cloneable{

    private final User user;
    private final Statistic parent;

    public StatValue(final User user,
                     final Statistic parent){
        this(user, parent, parent==null ? 0 : parent.getDefaultValue());
    }

    public StatValue(final User user,
                     final Statistic parent,
                     final int value){
        this.user=user;
        this.parent=parent;
        this.value=value;
    }

    public User getUser(){
        return this.user;
    }

    public Statistic getParent(){
        return this.parent;
    }

    public int getValue(){
        return this.value;
    }

    public void setValue(final int value){
        Preconditions.checkArgument(value>=0, "value cannot be less than 0");
        this.value=value;
    }

    @Override
    public StatValue clone(){
        try{
            return (StatValue) super.clone();
        }catch(final CloneNotSupportedException e){
            e.printStackTrace();
        }
        return null;
    }

    private int value;
}
