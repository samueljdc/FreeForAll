package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SaveKitCommand implements CommandExecutor {

    private FreeForAll plugin = null;
    public SaveKitCommand(FreeForAll plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {

        if (!command.getName().equalsIgnoreCase("stats")) {

        } else if (!(commandSender instanceof Player)) {

        } else if (!(commandSender.hasPermission("freeforall.command.savekit"))) {

        } else {

        }

        return false;
    }
}
