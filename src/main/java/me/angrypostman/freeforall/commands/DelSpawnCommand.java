package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.util.Message;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static me.angrypostman.freeforall.FreeForAll.doAsync;
import static me.angrypostman.freeforall.FreeForAll.doSync;

public class DelSpawnCommand implements CommandExecutor{

    private FreeForAll plugin=null;
    private DataStorage dataStorage=null;

    public DelSpawnCommand(FreeForAll plugin){
        this.plugin=plugin;
        this.dataStorage=plugin.getDataStorage();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args){

        if(!command.getName().equalsIgnoreCase("delspawn")) return false;

        if(!(commandSender instanceof Player) || !commandSender.hasPermission("freeforall.command.delspawn")){
            Message.get("no-permission-message").send(commandSender);
            return true;
        }

        Player player=(Player) commandSender;

        if(args.length < 1){
            Message.get("correct-usage-message").replace("%commandName%",
                    command.getName()).replace("%usage%",
                    command.getUsage()).send(commandSender);
            return true;
        }

        int spawnId=0;
        try{
            spawnId=(Integer.parseInt(args[0]) - 1); //Arrays start at 0
        } catch(NumberFormatException ex){
            player.sendMessage(ChatColor.RED + "Please enter a valid spawn id.");
            return true;
        }

        List<Location> locations=dataStorage.getLocations();
        if(spawnId < 0 || locations.size() > spawnId){
            Message.get("invalid-spawn-id-message").send(commandSender);
            return true;
        }

        Location location=locations.get(spawnId);

        int finalSpawnId=spawnId;
        doAsync(() -> {
            dataStorage.deleteLocation(finalSpawnId);
            doSync(() -> {
                Message.get("spawn-deleted-message").send(player);
            });
        });

        return true;
    }
}
