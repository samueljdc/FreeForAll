package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.kit.FFAKit;
import me.angrypostman.freeforall.kit.KitManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class DelKitCommand implements CommandExecutor {

    private FreeForAll plugin = null;
    public DelKitCommand(FreeForAll plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (!command.getName().equalsIgnoreCase("delkit")) return false;

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to perform this command.");
            return true;
        }

        if (!commandSender.hasPermission("freeforall.command.delkit")) {
            commandSender.sendMessage(ChatColor.RED + "You don't have permission to perform this command.");
            return true;
        }

        Player player = (Player) commandSender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Correct usage: /delkit <name>");
            return false;
        }

        String kitName = args[0];
        Optional<FFAKit> kitOptional = KitManager.getKit(kitName);

        if (!kitOptional.isPresent()) {
            player.sendMessage(ChatColor.RED + "Unknown kit '" + kitName + "'");
            return true;
        }

        FFAKit kit = kitOptional.get();
        KitManager.deleteKit(kit);

        player.sendMessage(ChatColor.RED+kit.getName()+" has been deleted.");

        return false;
    }
}
