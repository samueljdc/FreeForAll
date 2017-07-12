package me.angrypostman.freeforall.listeners;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.util.Configuration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.ArrayList;
import java.util.List;

import static me.angrypostman.freeforall.FreeForAll.doSyncRepeating;

public class EnvironmentListeners implements Listener {

    private List<BlockState> blockList = new ArrayList<>();
    private List<BlockState> blockPlaceList = new ArrayList<>();

    private FreeForAll plugin = null;
    private Configuration config = null;

    public EnvironmentListeners(FreeForAll plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();

        doSyncRepeating(() -> {

            blockPlaceList.forEach(blockState -> {

                Block block = blockState.getBlock();
                Location location = block.getLocation();

                Block blockAt = location.getBlock();
                BlockState blockStateAt = blockAt.getState();
                blockAt.setType(Material.AIR);
                blockStateAt.update(true);

            });

            blockList.forEach(blockState -> {

                Block block = blockState.getBlock();
                Location location = block.getLocation();

                Block blockAt = location.getBlock();
                BlockState blockStateAt = blockAt.getState();
                blockAt.setType(block.getType());
                blockAt.setData(block.getData());
                blockStateAt.update(true);

            });

        }, 0L, 20L * 60L);

    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(config.disableWeatherChange());
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(config.disableCreatureSpawn());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(config.disableBlockPlace());
        if (!config.disableBlockPlace()) {
            Block block = event.getBlock();
            blockPlaceList.add(block.getState());
        }
    }

    @EventHandler
    public void onBlockDestroy(BlockBreakEvent event) {
        event.setCancelled(config.disableBlockDestroy());
        if (!config.disableBlockDestroy()) {
            Block block = event.getBlock();
            blockList.add(block.getState());
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (config.disableExplosionDestroy()) {
            event.blockList().clear();
            return;
        }

        event.blockList().forEach(block -> blockList.add(block.getState()));

    }

}
