package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserData;
import me.angrypostman.freeforall.user.UserManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

import static me.angrypostman.freeforall.FreeForAll.doAsync;
import static me.angrypostman.freeforall.FreeForAll.doSync;

public class ResetStatsCommand implements CommandExecutor{

    private FreeForAll plugin = null;
    public ResetStatsCommand(FreeForAll plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {

        if (!command.getName().equalsIgnoreCase("resetstats")) return false;

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to perform this command.");
            return true;
        }

        if (!commandSender.hasPermission("freeforall.command.resetstats")){
            commandSender.sendMessage(ChatColor.RED+"You don't have permission to perform this command.");
            return true;
        }

        Player player = (Player) commandSender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED+"Correct Usage: /resetstats <player>");
            return false;
        }

        doAsync(() -> {

            String lookupName = args[0].toLowerCase();
            Optional<User> optional = UserManager.getUser(lookupName);
            if (!optional.isPresent()) {
                doSync(() -> player.sendMessage(ChatColor.RED + "Failed to find " + lookupName + " in database records."));
                return;
            }

            User user = optional.get();
            UserData data = user.getUserData();
            data.resetStats();

            plugin.getDataStorage().saveUser(user);

            doSync(() -> player.sendMessage(ChatColor.GREEN+user.getName()+"'s stats have been reset."));

        });

        return false;
    }
}
