package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.data.YamlStorage;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserData;
import me.angrypostman.freeforall.util.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.IntStream;

import static me.angrypostman.freeforall.FreeForAll.doAsync;
import static me.angrypostman.freeforall.FreeForAll.doSync;

public class LeaderboardCommand implements CommandExecutor{

    private FreeForAll plugin=null;
    private DataStorage dataStorage;

    public LeaderboardCommand(FreeForAll plugin){
        this.plugin=plugin;
        this.dataStorage=plugin.getDataStorage();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args){

        if(!command.getName().equalsIgnoreCase("leaderboard")) return false;

        if(!(commandSender instanceof Player)||!commandSender.hasPermission("freeforall.command.leaderboard")){
            Message.get("no-permission-message").send(commandSender);
            return true;
        }

        Player player=(Player) commandSender;

        if(plugin.getDataStorage() instanceof YamlStorage){
            player.sendMessage(ChatColor.RED+"The leaderboard command is currently only supported while using MySQL or SQLite :/.");
            return true;
        }

        int page=1;
        if(args.length > 0){
            try{
                page=Integer.parseInt(args[0]);
            } catch(NumberFormatException ex){
                player.sendMessage(ChatColor.RED + "Please enter a valid page number.");
                return true;
            }
        }

        if(page < 1) page=1;

        int finalPage=page;

        doAsync(() -> { //Have to go out of the main thread as getLeaderboardTop can call MySQL

            List<User> leaderboard=dataStorage.getLeaderboardTop(finalPage);
            doSync(() -> { //No point in running back to the main thread for each result, we already have the data

                IntStream.range(0, leaderboard.size())
                        .forEachOrdered(index -> {

                            User user=leaderboard.get(index);
                            UserData userData=user.getUserData();
                            Message.get("leaderboard-format")
                                    .replace("%position%",index+1)
                                    .replace("%user%",user.getName())
                                    .replace("%points%", userData.getPoints().getValue())
                                    .send(player);

                        });

            });

        });

        return true;
    }
}
