package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserCache;
import me.angrypostman.freeforall.user.UserData;
import me.angrypostman.freeforall.util.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

import static me.angrypostman.freeforall.FreeForAll.doAsync;
import static me.angrypostman.freeforall.FreeForAll.doSync;

public class StatsCommand implements CommandExecutor{

    private FreeForAll plugin=null;
    private DataStorage storage=null;
    public StatsCommand(FreeForAll plugin){
        this.plugin=plugin;
        this.storage=plugin.getDataStorage();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args){

        if(!command.getName().equalsIgnoreCase("stats")) return false;

        if(!(commandSender instanceof Player)||!commandSender.hasPermission("freeforall.command.stats")){
            Message.get("no-permission-message").send(commandSender);
            return true;
        }

        Player player=(Player) commandSender;

        if(args.length >= 1 && player.hasPermission("freeforall.command.stats.viewOther")){

            doAsync(() -> {

                String lookupName=args[0].toLowerCase();
                Optional<User> tempUser=UserCache.getUser(lookupName);
                if(!tempUser.isPresent()){
                    doSync(() -> doSync(() -> Message.get("player-not-found-message")
                            .replace("%player%", lookupName)
                            .send(player)));
                    return;
                }

                User user=tempUser.get();
                UserData userData=user.getUserData();

                //Add the user back to the cache so we don't have to query the database
                //(user data expires every 10 minutes)
                if(!user.isOnline()) UserCache.cacheUser(user);

                doSync(() -> {

                    Message.get("player-stats-heading")
                            .replace("%player%", user.getName())
                            .send(player);
                    Message.get("player-stats-format")
                            .replace("%statistic%", userData.getPoints().getParent().getFriendlyName())
                            .replace("%value%", userData.getPoints().getValue())
                            .send(player);
                    Message.get("player-stats-format")
                            .replace("%statistic%", userData.getKills().getParent().getFriendlyName())
                            .replace("%value%", userData.getKills().getValue())
                            .send(player);
                    Message.get("player-stats-format")
                            .replace("%statistic%", userData.getDeaths().getParent().getFriendlyName())
                            .replace("%value%", userData.getDeaths().getValue())
                            .send(player);
                    Message.get("player-stats-format")
                            .replace("%statistic%", userData.getKillStreak().getParent().getFriendlyName())
                            .replace("%value%", userData.getKillStreak().getValue())
                            .send(player);
                    Message.get("player-stats-footer").send(player);

                });

            });

        } else{


            Optional<User> tempUser=UserCache.getUserIfPresent(player.getUniqueId());
            if(!tempUser.isPresent()){
                player.sendMessage(ChatColor.RED + "Failed to load your player data, please relog.");
                return true;
            }

            User user=tempUser.get();
            UserData userData=user.getUserData();

            Message.get("player-stats-heading")
                    .replace("%player%", user.getName())
                    .send(player);
            Message.get("player-stats-format")
                    .replace("%statistic%", userData.getPoints().getParent().getFriendlyName())
                    .replace("%value%", userData.getPoints().getValue())
                    .send(player);
            Message.get("player-stats-format")
                    .replace("%statistic%", userData.getKills().getParent().getFriendlyName())
                    .replace("%value%", userData.getKills().getValue())
                    .send(player);
            Message.get("player-stats-format")
                    .replace("%statistic%", userData.getDeaths().getParent().getFriendlyName())
                    .replace("%value%", userData.getDeaths().getValue())
                    .send(player);
            Message.get("player-stats-format")
                    .replace("%statistic%", userData.getKillStreak().getParent().getFriendlyName())
                    .replace("%value%", userData.getKillStreak().getValue())
                    .send(player);
            Message.get("player-stats-footer").send(player);

        }

        return true;
    }

}
