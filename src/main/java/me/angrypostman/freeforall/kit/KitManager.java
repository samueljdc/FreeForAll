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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class KitManager {

    private static FreeForAll plugin = null;
    private static Configuration config = null;
    private static List<FFAKit> kits = null;
    private static Map<UUID, FFAKit> playerKits = null;

    static {
        plugin = FreeForAll.getPlugin();
        config = plugin.getConfiguration();
        kits = new ArrayList<>();
        playerKits = new HashMap<>();
    }

    public static void loadKits() {

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("kits");

        if (section == null || section.getKeys(false).size() == 0) return;

        List<FFAKit> kits = new ArrayList<>();

        for (String kit : section.getKeys(false)) {

            String friendly = section.getString(kit + ".friendly");
            String permission = section.getString(kit + ".permission");

            Material helmet = Material.getMaterial(section.getString(kit + ".helmet"));
            Material chestplate = Material.getMaterial(section.getString(kit + ".chestplate"));
            Material leggings = Material.getMaterial(section.getString(kit + ".leggings"));
            Material boots = Material.getMaterial(section.getString(kit + ".boots"));

            List<ItemStack> deserialized = new ArrayList<>();
            for (String stack : section.getStringList(kit + ".contents")) {

                String[] parts = stack.split(",");

                Material material = Material.getMaterial(parts[0]);
                Byte data = Byte.parseByte(parts[1]);
                Integer amount = Integer.parseInt(parts[2]);

                ItemStack itemStack = new ItemStack(material.getId(), amount, (short) 1, data);
                deserialized.add(itemStack);

            }

            List<PotionEffect> potionEffects = new ArrayList<>();
            for (String potionEffect : section.getStringList(kit + ".potionEffects")) {

                String[] parts = potionEffect.split(",");

                PotionEffectType potionEffectType = PotionEffectType.getByName(parts[0]);
                Integer duration = Integer.parseInt(parts[1]);
                Integer amplifier = Integer.parseInt(parts[2]);

                PotionEffect potion = new PotionEffect(potionEffectType, duration, amplifier);
                potionEffects.add(potion);

            }

            FFAKit ffaKit = new FFAKit(friendly, permission);
            if (helmet != null) ffaKit.setHelmet(helmet);
            if (chestplate != null) ffaKit.setChestplate(chestplate);
            if (leggings != null) ffaKit.setLeggings(leggings);
            if (boots != null) ffaKit.setBoots(boots);

            ffaKit.setInventoryItems(deserialized);
            ffaKit.setPotionEffects(potionEffects);

            kits.add(ffaKit);

        }

        KitManager.kits.addAll(kits);

    }

    public static Optional<FFAKit> getKitOf(Player player) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkArgument(player.isOnline(), "player not online");
        return playerKits.entrySet().stream()
                .filter(entry -> entry.getKey().equals(player.getUniqueId()))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public static Optional<FFAKit> getKit(String name) {
        Preconditions.checkArgument(name != null && !name.isEmpty(), "kit name cannot be null or effectively null");
        return kits.stream().filter(kit -> kit.getName().equalsIgnoreCase(name)).findFirst();
    }

    public static Optional<FFAKit> getDefaultKit() {
        String defaultKit = config.getDefaultKit();
        if (defaultKit == null || defaultKit.isEmpty()) {
            return Optional.empty();
        }
        return getKit(defaultKit);
    }

    public static void giveItems(User user, FFAKit kit) {

        Preconditions.checkNotNull(user, "user");
        Preconditions.checkNotNull(kit, "kit");

        Player player = user.getBukkitPlayer();

        Preconditions.checkNotNull(player, "player");
        Preconditions.checkArgument(player.isOnline(), "player not online");

        if (kit.hasPermission() && !player.hasPermission(kit.getPermission())) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this kit!");
            return;
        }

        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        PlayerInventory inventory = player.getInventory();
        inventory.setArmorContents(null);
        inventory.clear();

        if (kit.getHelmet() != null) inventory.setHelmet(kit.getHelmet());
        if (kit.getChestplate() != null) inventory.setChestplate(kit.getChestplate());
        if (kit.getLeggings() != null) inventory.setLeggings(kit.getLeggings());
        if (kit.getBoots() != null) inventory.setBoots(kit.getBoots());

        kit.getInventoryItems().forEach(inventory::addItem);
        kit.getPotionEffects().forEach(player::addPotionEffect);

        playerKits.put(player.getUniqueId(), kit);

        player.updateInventory();

    }

    public static void registerKit(FFAKit kit) {
        Preconditions.checkNotNull(kit, "kit");
        Preconditions.checkArgument(getKit(kit.getName()) == null, "kit already defined");
        KitManager.kits.add(kit);
    }

    public static void saveKit(FFAKit kit) {

        Preconditions.checkNotNull(kit, "kit");

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("kits");
        if (section == null) section = config.createSection("kits");

        String lowerCase = kit.getName().toLowerCase();

        section.set(lowerCase + ".friendly", kit.getName());
        section.set(lowerCase + ".permission", kit.getPermission());

        if (kit.getHelmet() != null)
            section.set(lowerCase + ".helmet", kit.getHelmet().getType().name());
        if (kit.getChestplate() != null)
            section.set(lowerCase + ".chestplate", kit.getChestplate().getType().name());
        if (kit.getLeggings() != null)
            section.set(lowerCase + ".leggings", kit.getLeggings().getType().name());
        if (kit.getBoots() != null)
            section.set(lowerCase + ".boots", kit.getBoots().getType().name());

        List<String> serializedStacks = new ArrayList<>();
        for (ItemStack stack : kit.getInventoryItems()) {
            if (stack == null || stack.getType() == Material.AIR) continue;

            Material material = stack.getType();
            String name = material.name();

            Byte data = stack.getData().getData();
            Integer amount = stack.getAmount();

            String newStack = name + "," + data + "," + amount;
            serializedStacks.add(newStack);

        }

        List<String> seralizedPotions = new ArrayList<>();
        for (PotionEffect potionEffect : kit.getPotionEffects()) {

            PotionEffectType potionEffectType = potionEffect.getType();
            Integer duration = potionEffect.getDuration();
            Integer amplifier = potionEffect.getAmplifier();

            String serializedPotion = potionEffectType.getName() + "," + duration + "," + amplifier;
            seralizedPotions.add(serializedPotion);

        }

        section.set(lowerCase + ".contents", serializedStacks);
        section.set(lowerCase + ".potionEffects", seralizedPotions);
        plugin.saveConfig();

    }

    public static List<FFAKit> getKits() {
        return new ArrayList<>(kits);
    }

    public static void deleteKit(FFAKit kit) {

        Preconditions.checkNotNull(kit, "cannot delete an unknown kit");

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("kits");
        if (section == null) return;

        KitManager.kits.remove(kit);
        section.set(kit.getName().toLowerCase(), null);
        plugin.saveConfig();
    }
}
