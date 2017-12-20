package me.angrypostman.freeforall.commands;

import java.util.Optional;
import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserCache;
import me.angrypostman.freeforall.user.UserData;
import me.angrypostman.freeforall.util.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.angrypostman.freeforall.FreeForAll.doAsync;
import static me.angrypostman.freeforall.FreeForAll.doSync;

public class ResetStatsCommand implements CommandExecutor{

    public ResetStatsCommand(final FreeForAll plugin){
        this.plugin=plugin;
    }

    @Override
    public boolean onCommand(final CommandSender commandSender,
                             final Command command,
                             final String commandLabel,
                             final String[] args){

        if(!command.getName()
                   .equalsIgnoreCase("resetstats")){ return false; }

        if(!(commandSender instanceof Player)||!commandSender.hasPermission("freeforall.command.resetstats")){
            Message.get("no-permission-message")
                   .send(commandSender);
            return true;
        }

        final Player player=(Player) commandSender;

        if(args.length<1){
            Message.get("correct-usage-message")
                   .replace("%commandName%", command.getName())
                   .replace("%usage%", command.getUsage())
                   .send(player);
            return true;
        }

        doAsync(()->{

            final String lookupName=args[0].toLowerCase();
            final Optional<User> optional=UserCache.getUser(lookupName);
            if(!optional.isPresent()){
                doSync(()->Message.get("player-not-found-message")
                                  .replace("%player%", lookupName)
                                  .send(player));
                return;
            }

            final User user=optional.get();
            final UserData data=user.getUserData();
            data.resetStats();

            this.plugin.getDataStorage()
                       .saveUser(user);

            doSync(()->Message.get("player-stats-reset-message")
                              .replace("%player%", user.getName())
                              .send(player));
        });

        return true;
    }

    private FreeForAll plugin=null;
}
