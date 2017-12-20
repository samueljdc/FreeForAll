package me.angrypostman.freeforall.commands;

import java.util.Optional;
import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.kit.FFAKit;
import me.angrypostman.freeforall.kit.KitManager;
import me.angrypostman.freeforall.user.UserCache;
import me.angrypostman.freeforall.util.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelKitCommand implements CommandExecutor{

    public DelKitCommand(final FreeForAll plugin){
        this.plugin=plugin;
    }

    @Override
    public boolean onCommand(final CommandSender commandSender,
                             final Command command,
                             final String label,
                             final String[] args){

        if(!command.getName()
                   .equalsIgnoreCase("delkit")){ return false; }

        if(!(commandSender instanceof Player)||!commandSender.hasPermission("freeforall.command.delkit")){
            Message.get("no-permission-message")
                   .send(commandSender);
            return true;
        }

        final Player player=(Player) commandSender;

        if(UserCache.isSpectating(player)){
            Message.get("no-permission-while-spectating")
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
        KitManager.deleteKit(kit);
        Message.get("kit-deleted-message")
               .replace("%kitName%", kit.getName())
               .send(player);
        return true;
    }

    private FreeForAll plugin=null;
}
