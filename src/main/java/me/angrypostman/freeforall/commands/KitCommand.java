package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand implements CommandExecutor {

    private FreeForAll plugin = null;
    private DataStorage storage = null;
    public KitCommand(FreeForAll plugin) {
        this.plugin = plugin;
        this.storage = plugin.getDataStorage();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {

        if (!command.getName().equalsIgnoreCase("stats")) {

        } else if (!(commandSender instanceof Player)) {

        } else if (!(commandSender.hasPermission("freeforall.command.kit"))) {

        } else {

        }

        return false;
    }
}
