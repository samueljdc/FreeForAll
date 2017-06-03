package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;

import me.angrypostman.freeforall.kit.FFAKit;
import me.angrypostman.freeforall.kit.KitManager;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class KitCommand implements CommandExecutor {

    private FreeForAll plugin = null;
    private DataStorage storage = null;
    public KitCommand(FreeForAll plugin) {
        this.plugin = plugin;
        this.storage = plugin.getDataStorage();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {

        if (!command.getName().equalsIgnoreCase("kit")) return false;

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to perform this command.");
            return true;
        }

        if (!commandSender.hasPermission("freeforall.command.kit")) {
            commandSender.sendMessage(ChatColor.RED+"You don't have permission to perform this command.");
            return true;
        }

        Player player = (Player) commandSender;
        Optional<User> userOptional = UserManager.getUser(player);

        if (!userOptional.isPresent()) {
            player.sendMessage(ChatColor.RED+"Failed to load player data, please relog");
            return true;
        }

        User user = userOptional.get();

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED+"Correct usage: /kit <name>");
            return false;
        }

        String kitName = args[0].toLowerCase();
        Optional<FFAKit> kitOptional = KitManager.getKit(kitName);

        if (!kitOptional.isPresent()) {
            player.sendMessage(ChatColor.RED+"Unknown kit '"+kitName+"'");
            return true;
        }

        FFAKit kit = kitOptional.get();

        if (kit.hasPermission() && !player.hasPermission(kit.getPermission())) {
            player.sendMessage(ChatColor.RED+"You don't have permission for that kit.");
            return true;
        }

        KitManager.giveItems(user, kit);
        player.sendMessage(ChatColor.GREEN+"You have been given the '"+kitName+"' kit");
        return true;
    }
}
