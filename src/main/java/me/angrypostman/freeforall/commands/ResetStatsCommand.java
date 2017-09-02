package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserCache;
import me.angrypostman.freeforall.user.UserData;
import me.angrypostman.freeforall.util.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

import static me.angrypostman.freeforall.FreeForAll.doAsync;
import static me.angrypostman.freeforall.FreeForAll.doSync;

public class ResetStatsCommand implements CommandExecutor{

    private FreeForAll plugin=null;

    public ResetStatsCommand(FreeForAll plugin){
        this.plugin=plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args){

        if(!command.getName().equalsIgnoreCase("resetstats")) return false;

        if(!(commandSender instanceof Player)||!commandSender.hasPermission("freeforall.command.resetstats")){
            Message.get("no-permission-message").send(commandSender);
            return true;
        }

        Player player=(Player) commandSender;

        if(args.length < 1){
            Message.get("correct-usage-message")
                    .replace("%commandName%", command.getName())
                    .replace("%usage%", command.getUsage())
                    .send(player);
            return true;
        }

        doAsync(() -> {

            String lookupName=args[0].toLowerCase();
            Optional<User> optional=UserCache.getUser(lookupName);
            if(!optional.isPresent()){
                doSync(() -> Message.get("player-not-found-message")
                        .replace("%player%", lookupName)
                        .send(player));
                return;
            }

            User user=optional.get();
            UserData data=user.getUserData();
            data.resetStats();

            plugin.getDataStorage().saveUser(user);

            doSync(() -> Message.get("player-stats-reset-message")
                            .replace("%player%", user.getName())
                            .send(player));

        });

        return true;
    }
}
