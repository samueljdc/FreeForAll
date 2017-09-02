package me.angrypostman.freeforall.commands;

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

import java.util.Optional;

public class KitCommand implements CommandExecutor{

    private FreeForAll plugin=null;
    private DataStorage storage=null;

    public KitCommand(FreeForAll plugin){
        this.plugin=plugin;
        this.storage=plugin.getDataStorage();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args){

        if(!command.getName().equalsIgnoreCase("kit")) return false;

        if(!(commandSender instanceof Player)||!commandSender.hasPermission("freeforall.command.kit")){
            Message.get("no-permission-message").send(commandSender);
            return true;
        }

        Player player=(Player) commandSender;
        Optional<User> userOptional=UserCache.getUserIfPresent(player);

        if(!userOptional.isPresent()){
            player.sendMessage(ChatColor.RED + "Failed to load player data, please relog");
            return true;
        }

        User user=userOptional.get();

        if (Combat.hasBeenInCombat(user)){
            Message.get("combat-kit-change-message")
                    .send(player);
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

        if(kit.hasPermission() && !player.hasPermission(kit.getPermission())){
            Message.get("no-permission-message").send(player);
            return true;
        }

        KitManager.giveItems(user, kit);
        Message.get("kit-given-message")
                .replace("%kitName%", kit.getName())
                .send(player);
        return true;
    }
}
