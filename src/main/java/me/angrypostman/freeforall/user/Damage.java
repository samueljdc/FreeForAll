package me.angrypostman.freeforall.user;

public class Damage{

    private User damagee=null;
    private User damager=null;
    private double damage=0.0D;
    private long timestamp=0L;

    public Damage(User damagee, User damager, double damage){
        this.damagee=damagee;
        this.damager=damager;
        this.damage=damage;
        this.timestamp=System.currentTimeMillis();
    }

    public User getDamagee(){
        return damagee;
    }

    public User getDamager(){
        return damager;
    }

    public double getDamage(){
        return damage;
    }

    public long getTimestamp(){
        return timestamp;
    }

}
