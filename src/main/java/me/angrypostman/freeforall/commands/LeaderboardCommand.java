package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;

import static me.angrypostman.freeforall.FreeForAll.doAsync;
import static me.angrypostman.freeforall.FreeForAll.doSync;

public class LeaderboardCommand implements CommandExecutor {

    private FreeForAll plugin = null;
    private DataStorage dataStorage;

    public LeaderboardCommand(FreeForAll plugin) {
        this.plugin = plugin;
        this.dataStorage = plugin.getDataStorage();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {

        if (!command.getName().equalsIgnoreCase("leaderboard")) return false;

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to perform this command.");
            return true;
        }

        if (!commandSender.hasPermission("freeforall.command.leaderboard")) {
            commandSender.sendMessage(ChatColor.RED + "You don't have permission to perform this command.");
            return true;
        }

        Player player = (Player) commandSender;

        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                player.sendMessage(ChatColor.RED + "Please enter a valid page number.");
                return false;
            }
        }

        if (page < 1) page = 1;

        int finalPage = page;
        doAsync(() -> { //Have to go out of the main thread as getLeaderboardTop will (may) call MySQL
            List<User> leaderboard = dataStorage.getLeardboardTop(finalPage);
            doSync(() -> { //No point in running back to the main thread for each result, we already have the data
                int counter = 1;
                for (Iterator<User> iterator = leaderboard.iterator(); iterator.hasNext(); counter++) {

                    User user = iterator.next();
                    UserData data = user.getUserData();

                    player.sendMessage(ChatColor.GOLD + "" + counter + ". " + user.getName() + " (" + data.getPoints() + " points)");

                }
            });
        });

        return true;
    }
}
