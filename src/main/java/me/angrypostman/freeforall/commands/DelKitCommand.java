package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.kit.FFAKit;
import me.angrypostman.freeforall.kit.KitManager;
import me.angrypostman.freeforall.user.UserCache;
import me.angrypostman.freeforall.util.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class DelKitCommand implements CommandExecutor{

    private FreeForAll plugin=null;

    public DelKitCommand(FreeForAll plugin){
        this.plugin=plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args){

        if(!command.getName().equalsIgnoreCase("delkit")) return false;

        if(!(commandSender instanceof Player)||!commandSender.hasPermission("freeforall.command.delkit")){
            Message.get("no-permission-message").send(commandSender);
            return true;
        }

        Player player=(Player) commandSender;

        if (UserCache.isSpectating(player)){
            Message.get("no-permission-while-spectating").send(player);
            return true;
        }

        if(args.length < 1){
            Message.get("correct-usage-message")
                    .replace("%commandName%", command.getName())
                    .replace("%usage%", command.getUsage())
                    .send(player);
            return true;
        }

        String kitName=args[0];
        Optional<FFAKit> kitOptional=KitManager.getKit(kitName);

        if(!kitOptional.isPresent()){
            Message.get("unknown-kit-message")
                    .replace("%kitName%", kitName)
                    .send(player);
            return true;
        }

        FFAKit kit=kitOptional.get();
        KitManager.deleteKit(kit);
        Message.get("kit-deleted-message")
                .replace("%kitName%", kit.getName())
                .send(player);
        return true;
    }
}
