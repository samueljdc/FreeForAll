package me.angrypostman.freeforall.commands;

import java.util.List;
import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.UserCache;
import me.angrypostman.freeforall.util.Message;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.angrypostman.freeforall.FreeForAll.doAsync;
import static me.angrypostman.freeforall.FreeForAll.doSync;

public class DelSpawnCommand implements CommandExecutor{

    public DelSpawnCommand(final FreeForAll plugin){
        this.plugin=plugin;
        this.dataStorage=plugin.getDataStorage();
    }

    @Override
    public boolean onCommand(final CommandSender commandSender,
                             final Command command,
                             final String label,
                             final String[] args){

        if(!command.getName()
                   .equalsIgnoreCase("delspawn")){ return false; }

        if(!(commandSender instanceof Player)||!commandSender.hasPermission("freeforall.command.delspawn")){
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
                   .send(commandSender);
            return true;
        }

        int spawnId=0;
        try{
            spawnId=(Integer.parseInt(args[0])-1); //Arrays start at 0
        }catch(final NumberFormatException ex){
            player.sendMessage(ChatColor.RED+"Please enter a valid spawn id.");
            return true;
        }

        final List<Location> locations=this.dataStorage.getLocations();
        if(spawnId<0||locations.size()>spawnId){
            Message.get("invalid-spawn-id-message")
                   .send(commandSender);
            return true;
        }

        final Location location=locations.get(spawnId);

        final int finalSpawnId=spawnId;
        doAsync(()->{
            this.dataStorage.deleteLocation(finalSpawnId);
            doSync(()->{
                Message.get("spawn-deleted-message")
                       .send(player);
            });
        });

        return true;
    }

    private FreeForAll plugin=null;
    private DataStorage dataStorage=null;
}
