package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.angrypostman.freeforall.FreeForAll.doAsync;
import static me.angrypostman.freeforall.FreeForAll.doSync;

public class SetSpawnCommand implements CommandExecutor {

    private FreeForAll plugin;
    private DataStorage dataStorage;

    public SetSpawnCommand(FreeForAll plugin) {
        this.plugin = plugin;
        this.dataStorage = plugin.getDataStorage();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (!command.getName().equalsIgnoreCase("setspawn")) return false;

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to perform this command.");
            return false;
        }

        if (!commandSender.hasPermission("freeforall.command.setspawn")) {
            commandSender.sendMessage(ChatColor.RED + "You don't have permission to perform this command.");
            return true;
        }

        Player player = (Player) commandSender;
        Location playerLocation = player.getLocation();

        //TODO: Check for duplicate locations ?

        doAsync(() -> {

            dataStorage.saveLocation(playerLocation);

            doSync(() -> {
                player.sendMessage(ChatColor.GREEN + "Spawn set!");
            });

        });

        return false;
    }
}
