package me.angrypostman.freeforall.listeners;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.Combat;
import me.angrypostman.freeforall.user.Damage;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserCache;
import me.angrypostman.freeforall.util.Configuration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Optional;

public class EntityDamageListener implements Listener{

    private FreeForAll plugin=null;
    private Configuration config=null;
    private DataStorage storage=null;

    public EntityDamageListener(FreeForAll plugin){
        this.plugin=plugin;
        this.config=plugin.getConfiguration();
        this.storage=plugin.getDataStorage();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){

        Entity entity=event.getEntity();
        if (!(entity instanceof Player))return;

        Player player=(Player)entity;
        Optional<User> optional=UserCache.getUserIfPresent(player);

        if (!optional.isPresent())return;

        User user=optional.get();
        if (user.isSpectating()){
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageEvent event){

        if(!(event.getEntity() instanceof Player)) return;

        Player player=(Player) event.getEntity();
        Optional<User> optional=UserCache.getUserIfPresent(player);

        if(!optional.isPresent()) return;

        User user=optional.get();

        double finalDamage=event.getFinalDamage();

        if (user.isSpectating()){
            event.setCancelled(true);
            return;
        }

        if(!config.isPvPLogger() || event.isCancelled() || finalDamage == 0) return;

        if(event instanceof EntityDamageByEntityEvent){

            EntityDamageByEntityEvent evt=(EntityDamageByEntityEvent) event;

            Entity damager=evt.getDamager();
            Optional<User> attacker=Optional.empty();

            if(damager instanceof Player){
                attacker=UserCache.getUserIfPresent(damager.getUniqueId());
            } else if(damager instanceof Projectile){
                Projectile projectile=(Projectile) damager;
                if(projectile.getShooter() instanceof Player){
                    Player shooter = (Player) projectile.getShooter();
                    if (!shooter.getUniqueId().equals(player.getUniqueId())){
                        attacker=UserCache.getUserIfPresent(((Player) projectile.getShooter()).getUniqueId());
                    }
                }
            } else if(damager instanceof TNTPrimed){
                TNTPrimed tntPrimed=(TNTPrimed) damager;
                if(tntPrimed.getSource() != null && tntPrimed.getSource() instanceof Player && !tntPrimed.getSource().getUniqueId().equals(player.getUniqueId())){
                    attacker=UserCache.getUserIfPresent(tntPrimed.getSource().getUniqueId());
                }
            } else if(damager instanceof Tameable){
                Tameable tameable=(Tameable) damager;
                if(tameable.getOwner() != null && tameable.getOwner() instanceof Player &&
                        !tameable.getOwner().getUniqueId().equals(player.getUniqueId())){
                    attacker=UserCache.getUserIfPresent(tameable.getOwner().getUniqueId());
                }
            }

            if(attacker.isPresent()){
                User enemy = attacker.get();
                if (enemy.isSpectating()){
                    event.setCancelled(true);
                }else{
                    Damage damage=new Damage(user, enemy, finalDamage);
                    Combat.setLastDamage(user, damage);
                }
            }

        }


    }

}
