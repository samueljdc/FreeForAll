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

        double finalDamage = event.getFinalDamage();

        if (event.isCancelled() || finalDamage == 0) return;

        Player player = (Player) event.getEntity();
        User user = UserManager.getUser(player);

        if (Combat.isInvulnreble(user)) {
            event.setCancelled(true);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent) {

            EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent) event;

            Entity damager = evt.getDamager();
            User attacker = null;

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
            }

            if (attacker != null) {
                if (Combat.isInvulnreble(attacker))Combat.setInvulnreble(attacker, false);
                Damage damage = new Damage(user, attacker, finalDamage);
                Combat.setLastDamage(user, damage);
            }

        }



    }

}
