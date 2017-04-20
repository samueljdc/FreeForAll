package me.angrypostman.freeforall.kit;

import com.google.common.base.Preconditions;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;

import me.angrypostman.freeforall.util.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class KitManager {

    private static FreeForAll plugin = FreeForAll.getPlugin();
    private static Configuration config = plugin.getConfiguration();
    private static List<FFAKit> kits = new ArrayList<>();
    private static Map<UUID, FFAKit> playerKits = new HashMap<>();

    public static FFAKit getKitOf(Player player) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkArgument(player.isOnline(), "player not online");
        for (Map.Entry<UUID, FFAKit> entry : playerKits.entrySet()) {
            if (entry.getKey().equals(player.getUniqueId())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static FFAKit getKit(String name) {
        for (FFAKit kit : getKits()) {
            if (kit.getName().equalsIgnoreCase(name)) {
                return kit;
            }
        }
        return null;
    }

    public static FFAKit getDefaultKit() {
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

    public static List<FFAKit> getKits() {
        return kits;
    }
}
