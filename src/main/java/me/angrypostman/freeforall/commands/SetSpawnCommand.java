package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.util.Message;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.angrypostman.freeforall.FreeForAll.doAsync;
import static me.angrypostman.freeforall.FreeForAll.doSync;

public class SetSpawnCommand implements CommandExecutor{

    private FreeForAll plugin;
    private DataStorage dataStorage;
    public SetSpawnCommand(FreeForAll plugin){
        this.plugin=plugin;
        this.dataStorage=plugin.getDataStorage();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args){

        if(!command.getName().equalsIgnoreCase("setspawn")) return false;

        if(!(commandSender instanceof Player)||!commandSender.hasPermission("freeforall.command.setspawn")){
            Message.get("no-permission-message").send(commandSender);
            return true;
        }

        Player player=(Player) commandSender;

        Location playerLocation=player.getLocation();

        doAsync(() -> {
            dataStorage.saveLocation(playerLocation);
            doSync(()->Message.get("spawn-saved-message").send(player));
        });

        return true;
    }
}
