package me.angrypostman.freeforall.listeners;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.Combat;
import me.angrypostman.freeforall.user.Damage;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Optional;

public class EntityDamageListener implements Listener {

    private FreeForAll plugin = null;
    private DataStorage storage = null;
    public EntityDamageListener(FreeForAll plugin) {
        this.plugin = plugin;
        this.storage = plugin.getDataStorage();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Optional<User> optional = UserManager.getUser(player);

        if (!optional.isPresent()) {
            player.kickPlayer("Failed to load player data, please relog.");
            throw new IllegalArgumentException("failed to load player data for '"+player.getName()+"'");
        }

        User user = optional.get();

        double finalDamage = event.getFinalDamage();
        if (event.isCancelled() || finalDamage == 0) return;

        if (event instanceof EntityDamageByEntityEvent) {

            EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent) event;

            Entity damager = evt.getDamager();
            Optional<User> attacker = null;

            if (damager instanceof Player) {
                attacker = UserManager.getUser(damager.getUniqueId());
            } else if (damager instanceof Projectile) {
                Projectile projectile = (Projectile) damager;
                if (projectile.getShooter() instanceof Player) {
                    attacker = UserManager.getUser(((Player) projectile.getShooter()).getUniqueId());
                }
            } else if (damager instanceof TNTPrimed) {
                TNTPrimed tntPrimed = (TNTPrimed) damager;
                if (tntPrimed.getSource() != null
                        && tntPrimed.getSource() instanceof Player
                        && !tntPrimed.getSource().getUniqueId().equals(player.getUniqueId())) {
                    attacker = UserManager.getUser(tntPrimed.getSource().getUniqueId());
                }
            } else if (damager instanceof Tameable) {
                Tameable tameable = (Tameable) damager;
                if (tameable.getOwner() != null && tameable.getOwner() instanceof Player) {
                    attacker = UserManager.getUser(tameable.getOwner().getUniqueId());
                }
            }

            if (attacker != null && attacker.isPresent()) {
                Damage damage = new Damage(user, attacker.get(), finalDamage);
                Combat.setLastDamage(user, damage);
            }

        }



    }

}
