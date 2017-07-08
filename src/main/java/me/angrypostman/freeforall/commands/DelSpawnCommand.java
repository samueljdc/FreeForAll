package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static me.angrypostman.freeforall.FreeForAll.doAsync;
import static me.angrypostman.freeforall.FreeForAll.doSync;

public class DelSpawnCommand implements CommandExecutor {

    private FreeForAll plugin = null;
    private DataStorage dataStorage = null;

    public DelSpawnCommand(FreeForAll plugin) {
        this.plugin = plugin;
        this.dataStorage = plugin.getDataStorage();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        //if (!command.getName().equalsIgnoreCase("delspawn")) return false;

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to perform this command.");
            return true;
        }

        if (!commandSender.hasPermission("freeforall.command.delspawn")) {
            commandSender.sendMessage(ChatColor.RED + "You don't have permission to perform this command.");
            return true;
        }

        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Correct Usage: /delspawn <spawnId>");
            return false;
        }

        Player player = (Player) commandSender;

        int spawnId = 0;
        try {
            spawnId = (Integer.parseInt(args[0]) - 1); //Arrays start at 0
        } catch (NumberFormatException ex) {
            player.sendMessage(ChatColor.RED + "Please enter a valid spawnId.");
            return false;
        }

        List<Location> locations = dataStorage.getLocations();
        if (spawnId < 0 || locations.size() > spawnId) {
            player.sendMessage(ChatColor.RED + "Please enter a valid spawnId.");
            return false;
        }

        Location location = locations.get(spawnId);

        int finalSpawnId = spawnId;
        doAsync(() -> {

            dataStorage.deleteLocation(finalSpawnId);

            doSync(() -> {
                player.sendMessage(ChatColor.RED + "Spawn deleted!");
            });

        });

        return false;
    }
}
