package me.angrypostman.freeforall.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.kit.FFAKit;
import me.angrypostman.freeforall.kit.KitManager;
import me.angrypostman.freeforall.user.UserCache;
import me.angrypostman.freeforall.util.Message;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

public class SaveKitCommand implements CommandExecutor{

    public SaveKitCommand(final FreeForAll plugin){
        this.plugin=plugin;
    }

    @Override
    public boolean onCommand(final CommandSender commandSender,
                             final Command command,
                             final String commandLabel,
                             final String[] args){

        if(!command.getName()
                   .equalsIgnoreCase("savekit")){ return false; }

        if(!(commandSender instanceof Player)||!commandSender.hasPermission("freeforall.command.savekit")){
            Message.get("no-permission-message")
                   .send(commandSender);
            return true;
        }

        final Player player=(Player) commandSender;
        final PlayerInventory inventory=player.getInventory();

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

        final String name=args[0];
        final String permission=(args.length>1 ? args[1].toLowerCase() : null);
        if(permission!=null&&!permission.matches("([a-z]+\\.?)+")){
            Message.get("invalid-permission-node")
                   .send(player);
            return true;
        }

        if(permission!=null&&KitManager.getKits()
                                       .stream()
                                       .anyMatch(ffakit->ffakit.getPermission()
                                                               .equalsIgnoreCase(permission))){
            Message.get("kit-permission-nodes-unique-message")
                   .send(player);
            return true;
        }

        if(KitManager.getKit(name)
                     .isPresent()){
            Message.get("kit-already-exists-message")
                   .send(player);
            return true;
        }

        final FFAKit kit=new FFAKit(name, permission);
        kit.setHelmet(inventory.getHelmet());
        kit.setChestplate(inventory.getChestplate());
        kit.setLeggings(inventory.getLeggings());
        kit.setBoots(inventory.getBoots());

        final List<ItemStack> inventoryItems=new ArrayList<>();
        Arrays.stream(inventory.getStorageContents())
              .filter(stack->stack!=null&&stack.getType()!=Material.AIR)
              .forEach(inventoryItems::add);

        final List<PotionEffect> potionEffects=new ArrayList<>();
        potionEffects.addAll(player.getActivePotionEffects());

        kit.setInventoryItems(inventoryItems);
        kit.setPotionEffects(potionEffects);
        KitManager.saveKit(kit);

        Message.get("kit-created-message")
               .replace("%kitName%", kit.getName())
               .send(player);
        return true;
    }

    private FreeForAll plugin=null;
}
