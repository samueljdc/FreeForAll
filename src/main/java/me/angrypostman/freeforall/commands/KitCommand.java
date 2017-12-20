package me.angrypostman.freeforall.commands;

import java.util.Optional;
import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.kit.FFAKit;
import me.angrypostman.freeforall.kit.KitManager;
import me.angrypostman.freeforall.user.Combat;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserCache;
import me.angrypostman.freeforall.util.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand implements CommandExecutor{

    public KitCommand(final FreeForAll plugin){
        this.plugin=plugin;
        this.storage=plugin.getDataStorage();
    }

    @Override
    public boolean onCommand(final CommandSender commandSender,
                             final Command command,
                             final String commandLabel,
                             final String[] args){

        if(!command.getName()
                   .equalsIgnoreCase("kit")){ return false; }

        if(!(commandSender instanceof Player)||!commandSender.hasPermission("freeforall.command.kit")){
            Message.get("no-permission-message")
                   .send(commandSender);
            return true;
        }

        final Player player=(Player) commandSender;
        final Optional<User> userOptional=UserCache.getUserIfPresent(player);

        if(!userOptional.isPresent()){
            player.sendMessage(ChatColor.RED+"Failed to load your player data, please relog");
            return true;
        }

        final User user=userOptional.get();

        if(user.isSpectating()){
            Message.get("no-permission-while-spectating")
                   .send(player);
            return true;
        }

        if(Combat.hasBeenInCombat(user)){
            Message.get("combat-kit-change-message")
                   .send(player);
            return true;
        }

        if(args.length<1){
            Message.get("correct-usage-message")
                   .replace("%commandName%", command.getName())
                   .replace("%usage%", command.getUsage())
                   .send(player);
            return true;
        }

        final String kitName=args[0];
        final Optional<FFAKit> kitOptional=KitManager.getKit(kitName);

        if(!kitOptional.isPresent()){
            Message.get("unknown-kit-message")
                   .replace("%kitName%", kitName)
                   .send(player);
            return true;
        }

        final FFAKit kit=kitOptional.get();

        if(kit.hasPermission()&&!player.hasPermission(kit.getPermission())){
            Message.get("no-permission-message")
                   .send(player);
            return true;
        }

        KitManager.giveItems(user, kit);
        Message.get("kit-given-message")
               .replace("%kitName%", kit.getName())
               .send(player);
        return true;
    }

    private FreeForAll plugin=null;
    private DataStorage storage=null;
}
