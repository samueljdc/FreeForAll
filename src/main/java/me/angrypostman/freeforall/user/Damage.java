package me.angrypostman.freeforall.user;

public class Damage{

    private final User damagee;
    private final User damager;
    private final double damage;
    private final long timestamp;

    public Damage(final User damagee,
                  final User damager,
                  final double damage){
        this.damagee=damagee;
        this.damager=damager;
        this.damage=damage;
        this.timestamp=System.currentTimeMillis();
    }

    public User getDamagee(){
        return this.damagee;
    }

    public User getDamager(){
        return this.damager;
    }

    public double getDamage(){
        return this.damage;
    }

    public long getTimestamp(){
        return this.timestamp;
    }
}
