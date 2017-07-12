package me.angrypostman.freeforall.commands;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.kit.FFAKit;
import me.angrypostman.freeforall.kit.KitManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class SaveKitCommand implements CommandExecutor {

    private FreeForAll plugin = null;

    public SaveKitCommand(FreeForAll plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {

        if (!command.getName().equalsIgnoreCase("savekit")) return false;

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to perform this command.");
            return true;
        }

        if (!commandSender.hasPermission("freeforall.command.savekit")) {
            commandSender.sendMessage(ChatColor.RED + "You don't have permission to perform this command.");
            return true;
        }

        Player player = (Player) commandSender;

        PlayerInventory inventory = player.getInventory();

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Correct usage: /savekit <name> [permission]");
            return false;
        }

        String name = args[0];
        String permission = (args.length > 1 ? args[1].toLowerCase() : null);
        if (permission != null && !permission.matches("([a-z]+\\.?)+")) {
            player.sendMessage(ChatColor.RED + "Please enter a valid permission node.");
            return true;
        }

        if (permission != null && KitManager.getKits().stream().anyMatch(ffakit -> ffakit.getPermission().equalsIgnoreCase(permission))) {
            player.sendMessage(ChatColor.RED + "Permission nodes must be unique.");
            return true;
        }

        if (KitManager.getKit(name).isPresent()) {
            player.sendMessage(ChatColor.RED + "A kit already exists with that name");
            return true;
        }

        FFAKit kit = new FFAKit(name, permission);
        kit.setHelmet(inventory.getHelmet());
        kit.setChestplate(inventory.getChestplate());
        kit.setLeggings(inventory.getLeggings());
        kit.setBoots(inventory.getBoots());

        List<ItemStack> inventoryItems = new ArrayList<>();
        for (ItemStack stack : inventory.getStorageContents()) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            inventoryItems.add(stack);
        }

        List<PotionEffect> potionEffects = new ArrayList<>();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            potionEffects.add(effect);
        }

        kit.setInventoryItems(inventoryItems);
        kit.setPotionEffects(potionEffects);
        KitManager.registerKit(kit);
        KitManager.saveKit(kit);

        player.sendMessage(ChatColor.GREEN + kit.getName() + " has been created");
        return false;
    }
}
