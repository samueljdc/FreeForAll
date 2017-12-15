package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.kit.FFAKit;
import me.angrypostman.freeforall.kit.KitManager;
import me.angrypostman.freeforall.user.Combat;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserCache;
import me.angrypostman.freeforall.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class SpectateCommand implements CommandExecutor{

    private FreeForAll plugin;
    private DataStorage dataStorage;

    public SpectateCommand(FreeForAll plugin){
        this.plugin=plugin;
        this.dataStorage=plugin.getDataStorage();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){

        if(!command.getName().equalsIgnoreCase("spectate")) return false;

        if(!(sender instanceof Player) || !sender.hasPermission("freeforall.command.spectate")){
            Message.get("no-permission-message").send(sender);
            return true;
        }

        Player player=(Player) sender;
        Optional<User> optional=UserCache.getUserIfPresent(player);

        if(!optional.isPresent()){
            player.sendMessage(ChatColor.RED + "Failed to load your player data, please relog");
            return true;
        }

        User user=optional.get();
        if (user.isSpectating()){

            if (args.length > 0){
                Player target=Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()){
                    Message.get("player-not-found-message")
                            .replace("%player%", args[0])
                            .send(player);
                }
                player.teleport(target);
                return true;
            }

            Bukkit.getOnlinePlayers().forEach(online->{
                if (!online.canSee(player)){
                    online.showPlayer(player);
                }
            });

            Location location;

            List<Location> locations=dataStorage.getLocations();
            if(locations == null || locations.size() == 0){
                location=player.getWorld().getSpawnLocation();
            }else {
                location=locations.get(ThreadLocalRandom.current().nextInt(locations.size()));
            }

            player.teleport(location);
            player.setAllowFlight(false);
            player.setFlying(false);

            UserCache.setSpectating(user, false);

            Message.get("spectator-disabled-message").send(player);

            Optional<FFAKit> kitOptional=KitManager.getKitOf(player);
            if(!kitOptional.isPresent()){
                kitOptional=KitManager.getDefaultKit();
                if(!kitOptional.isPresent()) return true;
            }

            FFAKit ffaKit=kitOptional.get();
            KitManager.giveItems(user, ffaKit);

        } else {

            if (Combat.hasBeenInCombat(user)){
                Message.get("spectator-in-combat-message").send(player);
                return true;
            }

            Bukkit.getOnlinePlayers().forEach(online->{
                if(online.canSee(player)){
                    online.hidePlayer(player);
                }
            });

            PlayerInventory inventory=player.getInventory();
            inventory.setArmorContents(null);
            inventory.clear();

            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

            player.updateInventory();
            player.setAllowFlight(true);
            player.setFlying(true);

            if (args.length > 0){
                Player target=Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()){
                    Message.get("player-not-found-message")
                            .replace("%player%", args[0])
                            .send(player);
                }
                player.teleport(target);
            }

            UserCache.setSpectating(user, true);
            Message.get("spectator-enabled-message").send(player);

        }

        return true;
    }
}
