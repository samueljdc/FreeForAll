package me.angrypostman.freeforall.listeners;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.util.Configuration;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

import static me.angrypostman.freeforall.FreeForAll.doSyncLater;

public class GeneralListeners implements Listener {

    //A lot more events need to go here, just can't remember what ones....

    private FreeForAll plugin = null;
    public GeneralListeners(FreeForAll plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockDestroy(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        doSyncLater(() -> {

            Block clicked = event.getBlockClicked();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            BlockFace face = event.getBlockFace();
            Block friend = clicked.getRelative(face);

            if (friend == null || friend.getType() == Material.AIR) return;

            friend.setType(Material.AIR);

        }, 5 * 20L);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().clear(); //damage will still go off
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickup(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        player.setTotalExperience(0);
        event.setAmount(0);
    }

}
