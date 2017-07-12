package me.angrypostman.freeforall.runnables;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.util.Updater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

import static me.angrypostman.freeforall.FreeForAll.doSync;

public class UpdaterTask extends BukkitRunnable {

    private FreeForAll plugin = null;
    private Updater updater = null;
    public UpdaterTask(FreeForAll plugin) {
        this.plugin = plugin;
        this.updater = new Updater(plugin);
    }

    @Override
    public void run() {

        plugin.getLogger().info("Checking for updates...");

        try {
            updater.checkUpdate("v"+plugin.getConfiguration().getVersion());
        } catch (IOException ex) {
            plugin.getLogger().info("An error occurred whilst checking for updates.");
            plugin.getLogger().info("Message: "+ex.getMessage());
            this.cancel();
        }

        String latestVersion = updater.getLatestVersion();
        if (latestVersion != null) {
            plugin.getLogger().info("A new version of FreeForAll is available for download (v"+latestVersion+")!");

            doSync(() -> {
                Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(player -> {
                    player.sendMessage(ChatColor.GOLD+"A new version of FreeForAll is available for download!");
                });
            });

        } else {
            plugin.getLogger().info("FreeForAll is already up to date (currently running v" + plugin.getConfiguration().getVersion() + ")");
        }

    }
}
