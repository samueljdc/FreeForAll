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

    public SetSpawnCommand(final FreeForAll plugin){
        this.plugin=plugin;
        this.dataStorage=plugin.getDataStorage();
    }

    @Override
    public boolean onCommand(final CommandSender commandSender,
                             final Command command,
                             final String label,
                             final String[] args){

        if(!command.getName()
                   .equalsIgnoreCase("setspawn")){ return false; }

        if(!(commandSender instanceof Player)||!commandSender.hasPermission("freeforall.command.setspawn")){
            Message.get("no-permission-message")
                   .send(commandSender);
            return true;
        }

        final Player player=(Player) commandSender;

        final Location playerLocation=player.getLocation();

        doAsync(()->{
            this.dataStorage.saveLocation(playerLocation);
            doSync(()->Message.get("spawn-saved-message")
                              .send(player));
        });

        return true;
    }

    private final FreeForAll plugin;
    private final DataStorage dataStorage;
}
