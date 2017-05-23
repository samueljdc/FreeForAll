package me.angrypostman.freeforall.kit;

import com.google.common.base.Preconditions;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;

import me.angrypostman.freeforall.util.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class KitManager {

    private static FreeForAll plugin = FreeForAll.getPlugin();
    private static Configuration config = plugin.getConfiguration();
    private static List<FFAKit> kits = new ArrayList<>();
    private static Map<UUID, FFAKit> playerKits = new HashMap<>();

    public static Optional<FFAKit> getKitOf(Player player) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkArgument(player.isOnline(), "player not online");
        return playerKits.entrySet().stream()
                .filter(entry -> entry.getKey().equals(player.getUniqueId()))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public static Optional<FFAKit> getKit(String name) {
        return kits.stream().filter(kit -> kit.getName().equalsIgnoreCase(name)).findFirst();
    }

    public static Optional<FFAKit> getDefaultKit() {
        return getKit(config.getDefaultKit());
    }

    public static void giveItems(User user, FFAKit kit) {

        Preconditions.checkNotNull(user, "user");
        Preconditions.checkNotNull(kit, "kit");

        Player player = user.getBukkitPlayer();

        Preconditions.checkNotNull(player, "player");
        Preconditions.checkArgument(player.isOnline(), "player not online");

        if (kit.hasPermission()) {
            if (!player.hasPermission(kit.getPermission())) {
                player.sendMessage(ChatColor.RED+"You don't have permission to use this kit!");
            }
        }

        PlayerInventory inventory = player.getInventory();
        inventory.setArmorContents(null);
        inventory.clear();

        if (kit.getHelmet()!=null) inventory.setHelmet(kit.getHelmet());
        if (kit.getChestplate()!=null) inventory.setChestplate(kit.getChestplate());
        if (kit.getLeggings()!=null) inventory.setLeggings(kit.getLeggings());
        if (kit.getBoots()!=null) inventory.setBoots(kit.getBoots());

        kit.getInventoryItems().forEach(inventory::addItem);

        playerKits.put(player.getUniqueId(), kit);

        player.updateInventory();

    }

    public static void registerKit(FFAKit kit) {
        Preconditions.checkNotNull(kit, "kit");
        Preconditions.checkArgument(getKit(kit.getName())==null, "kit already defined");
        kits.add(kit);
    }

    public static void saveKit(FFAKit kit) {

        Preconditions.checkNotNull(kit, "kit");

        FileConfiguration config = FreeForAll.getPlugin().getConfig();
        ConfigurationSection section = config.getConfigurationSection("kits");
        if (section == null) section = config.createSection("kits");

        String lowerCase = kit.getName().toLowerCase();

        config.set("kits."+lowerCase+".friendly", kit.getName());

        if (kit.getHelmet() != null) config.set("kits."+lowerCase+".helmet", kit.getHelmet().getType().name());
        if (kit.getChestplate() != null) config.set("kits."+lowerCase+".chestplate", kit.getChestplate().getType().name());
        if (kit.getLeggings() != null) config.set("kits."+lowerCase+".leggings", kit.getLeggings().getType().name());
        if (kit.getBoots() != null) config.set("kits."+lowerCase+".boots", kit.getBoots().getType().name());

        List<String> serialized = new ArrayList<>();
        for (ItemStack stack : kit.getInventoryItems()) {
            if (stack == null || stack.getType()== Material.AIR) continue;

            Material material = stack.getType();
            String name = material.name();

            Byte data = stack.getData().getData();
            Integer amount = stack.getAmount();

            String newStack = name+","+data+","+amount;
            serialized.add(newStack);

        }

        config.set("kits."+lowerCase+".contents", serialized);
        FreeForAll.getPlugin().saveConfig();

    }

    public static List<FFAKit> getKits() {
        return new ArrayList<>(kits);
    }

}
