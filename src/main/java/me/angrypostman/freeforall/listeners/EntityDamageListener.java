package me.angrypostman.freeforall.listeners;

import java.util.Optional;
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

public class EntityDamageListener implements Listener{

    public EntityDamageListener(final FreeForAll plugin){
        this.plugin=plugin;
        this.config=plugin.getConfiguration();
        this.storage=plugin.getDataStorage();
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event){

        final Entity entity=event.getEntity();
        if(!(entity instanceof Player)){ return; }

        final Player player=(Player) entity;
        final Optional<User> optional=UserCache.getUserIfPresent(player);

        if(!optional.isPresent()){ return; }

        final User user=optional.get();
        if(user.isSpectating()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(final EntityDamageEvent event){

        if(!(event.getEntity() instanceof Player)){ return; }

        final Player player=(Player) event.getEntity();
        final Optional<User> optional=UserCache.getUserIfPresent(player);

        if(!optional.isPresent()){ return; }

        final User user=optional.get();

        final double finalDamage=event.getFinalDamage();

        if(user.isSpectating()){
            event.setCancelled(true);
            return;
        }

        if(!this.config.isPvPLogger()||event.isCancelled()||finalDamage==0){ return; }

        if(event instanceof EntityDamageByEntityEvent){

            final EntityDamageByEntityEvent evt=(EntityDamageByEntityEvent) event;

            final Entity damager=evt.getDamager();
            Optional<User> attacker=Optional.empty();

            if(damager instanceof Player){
                attacker=UserCache.getUserIfPresent(damager.getUniqueId());
            }else if(damager instanceof Projectile){
                final Projectile projectile=(Projectile) damager;
                if(projectile.getShooter() instanceof Player){
                    final Player shooter=(Player) projectile.getShooter();
                    if(!shooter.getUniqueId()
                               .equals(player.getUniqueId())){
                        attacker=UserCache.getUserIfPresent(((Player) projectile.getShooter()).getUniqueId());
                    }
                }
            }else if(damager instanceof TNTPrimed){
                final TNTPrimed tntPrimed=(TNTPrimed) damager;
                if(tntPrimed.getSource()!=null&&tntPrimed.getSource() instanceof Player&&!tntPrimed.getSource()
                                                                                                   .getUniqueId()
                                                                                                   .equals(player.getUniqueId())){
                    attacker=UserCache.getUserIfPresent(tntPrimed.getSource()
                                                                 .getUniqueId());
                }
            }else if(damager instanceof Tameable){
                final Tameable tameable=(Tameable) damager;
                if(tameable.getOwner()!=null&&tameable.getOwner() instanceof Player&&!tameable.getOwner()
                                                                                              .getUniqueId()
                                                                                              .equals(player.getUniqueId())){
                    attacker=UserCache.getUserIfPresent(tameable.getOwner()
                                                                .getUniqueId());
                }
            }

            if(attacker.isPresent()){
                final User enemy=attacker.get();
                if(enemy.isSpectating()){
                    event.setCancelled(true);
                }else{
                    final Damage damage=new Damage(user, enemy, finalDamage);
                    Combat.setLastDamage(user, damage);
                }
            }
        }
    }

    private FreeForAll plugin=null;
    private Configuration config=null;
    private DataStorage storage=null;
}
