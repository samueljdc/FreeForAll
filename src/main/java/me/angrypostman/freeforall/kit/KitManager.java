package me.angrypostman.freeforall.kit;

import com.google.common.base.Preconditions;
import java.util.*;
import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.util.Configuration;
import me.angrypostman.freeforall.util.Message;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class KitManager{

    private static FreeForAll plugin=null;
    private static Configuration config=null;
    private static List<FFAKit> kits=null;
    private static Map<UUID, FFAKit> playerKits=null;

    static{
        plugin=FreeForAll.getPlugin();
        config=plugin.getConfiguration();
        kits=new ArrayList<>();
        playerKits=new HashMap<>();
    }

    public static void loadKits(){

        KitManager.kits.clear();

        final FileConfiguration config=plugin.getConfig();
        final ConfigurationSection section=config.getConfigurationSection("kits");

        if(section==null||section.getKeys(false)
                                 .size()==0){ return; }

        final List<FFAKit> kits=new ArrayList<>();

        for(final String kit : section.getKeys(false)){

            final String friendly=section.getString(kit+".friendly");
            final String permission=section.getString(kit+".permission");

            final Material helmet=Material.getMaterial(section.getString(kit+".helmet"));
            final Material chestplate=Material.getMaterial(section.getString(kit+".chestplate"));
            final Material leggings=Material.getMaterial(section.getString(kit+".leggings"));
            final Material boots=Material.getMaterial(section.getString(kit+".boots"));

            final List<ItemStack> itemStacks=new ArrayList<>();
            for(final String stack : section.getStringList(kit+".contents")){

                final String[] parts=stack.split(",");

                final Material material=Material.getMaterial(parts[0]);
                final Byte data=Byte.parseByte(parts[1]);
                final Integer amount=Integer.parseInt(parts[2]);

                final ItemStack itemStack=new ItemStack(material.getId(), amount, (short) 1, data);
                itemStacks.add(itemStack);
            }

            final List<PotionEffect> potionEffects=new ArrayList<>();
            for(final String potionEffect : section.getStringList(kit+".potionEffects")){

                final String[] parts=potionEffect.split(",");

                final PotionEffectType potionEffectType=PotionEffectType.getByName(parts[0]);
                final Integer duration=Integer.parseInt(parts[1]);
                final Integer amplifier=Integer.parseInt(parts[2]);

                final PotionEffect potion=new PotionEffect(potionEffectType, duration, amplifier);
                potionEffects.add(potion);
            }

            final FFAKit ffaKit=new FFAKit(friendly, permission);
            if(helmet!=null){ ffaKit.setHelmet(helmet); }
            if(chestplate!=null){ ffaKit.setChestplate(chestplate); }
            if(leggings!=null){ ffaKit.setLeggings(leggings); }
            if(boots!=null){ ffaKit.setBoots(boots); }

            ffaKit.setInventoryItems(itemStacks);
            ffaKit.setPotionEffects(potionEffects);

            kits.add(ffaKit);
        }

        KitManager.kits.addAll(kits);
    }

    public static Optional<FFAKit> getKitOf(final User user){
        Preconditions.checkNotNull(user, "user cannot be null");
        Preconditions.checkArgument(user.isOnline(), "user is not online");
        return getKitOf(user.getBukkitPlayer());
    }

    public static Optional<FFAKit> getKitOf(final Player player){
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkArgument(player.isOnline(), "player not online");
        return playerKits.entrySet()
                         .stream()
                         .filter(entry->entry.getKey()
                                             .equals(player.getUniqueId()))
                         .map(Map.Entry::getValue)
                         .findFirst();
    }

    public static Optional<FFAKit> getDefaultKit(){
        final String defaultKit=config.getDefaultKit();
        if(defaultKit==null||defaultKit.isEmpty()){
            return Optional.empty();
        }
        return getKit(defaultKit);
    }

    public static Optional<FFAKit> getKit(final String name){
        Preconditions.checkArgument(name!=null&&!name.isEmpty(), "kit name cannot be null or effectively null");
        return kits.stream()
                   .filter(kit->kit.getName()
                                   .equalsIgnoreCase(name))
                   .findFirst();
    }

    public static void giveItems(final User user,
                                 final FFAKit kit){

        Preconditions.checkNotNull(user, "user cannot be null");
        Preconditions.checkNotNull(kit, "kit cannot be null");

        final Player player=user.getBukkitPlayer();

        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkArgument(player.isOnline(), "player not online");

        if(kit.hasPermission()&&!player.hasPermission(kit.getPermission())){
            Message.get("no-permission-message")
                   .send(player);
            return;
        }

        player.getActivePotionEffects()
              .forEach(potionEffect->player.removePotionEffect(potionEffect.getType()));

        final PlayerInventory inventory=player.getInventory();
        inventory.setArmorContents(null);
        inventory.clear();

        if(kit.getHelmet()!=null){ inventory.setHelmet(kit.getHelmet()); }
        if(kit.getChestplate()!=null){ inventory.setChestplate(kit.getChestplate()); }
        if(kit.getLeggings()!=null){ inventory.setLeggings(kit.getLeggings()); }
        if(kit.getBoots()!=null){ inventory.setBoots(kit.getBoots()); }

        kit.getInventoryItems()
           .forEach(inventory::addItem);
        kit.getPotionEffects()
           .forEach(player::addPotionEffect);

        playerKits.put(player.getUniqueId(), kit);

        player.updateInventory();
    }

    public static void saveKit(final FFAKit kit){

        Preconditions.checkNotNull(kit, "kit");

        final FileConfiguration config=plugin.getConfig();
        ConfigurationSection section=config.getConfigurationSection("kits");
        if(section==null){ section=config.createSection("kits"); }

        final String lowerCase=kit.getName()
                                  .toLowerCase();

        section.set(lowerCase+".friendly", kit.getName());
        section.set(lowerCase+".permission", kit.getPermission());

        if(kit.getHelmet()!=null){
            section.set(lowerCase+".helmet", kit.getHelmet()
                                                .getType()
                                                .name());
        }
        if(kit.getChestplate()!=null){
            section.set(lowerCase+".chestplate", kit.getChestplate()
                                                    .getType()
                                                    .name());
        }
        if(kit.getLeggings()!=null){
            section.set(lowerCase+".leggings", kit.getLeggings()
                                                  .getType()
                                                  .name());
        }
        if(kit.getBoots()!=null){
            section.set(lowerCase+".boots", kit.getBoots()
                                               .getType()
                                               .name());
        }

        final List<String> serializedStacks=new ArrayList<>();
        for(final ItemStack stack : kit.getInventoryItems()){
            if(stack==null||stack.getType()==Material.AIR){ continue; }

            final Material material=stack.getType();
            final String name=material.name();

            final Byte data=stack.getData()
                                 .getData();
            final Integer amount=stack.getAmount();

            final String newStack=name+","+data+","+amount;
            serializedStacks.add(newStack);
        }

        final List<String> seralizedPotions=new ArrayList<>();
        for(final PotionEffect potionEffect : kit.getPotionEffects()){

            final PotionEffectType potionEffectType=potionEffect.getType();
            final Integer duration=potionEffect.getDuration();
            final Integer amplifier=potionEffect.getAmplifier();

            final String serializedPotion=potionEffectType.getName()+","+duration+","+amplifier;
            seralizedPotions.add(serializedPotion);
        }

        section.set(lowerCase+".contents", serializedStacks);
        section.set(lowerCase+".potionEffects", seralizedPotions);
        plugin.saveConfig();

        if(!getKit(kit.getName()).isPresent()){
            registerKit(kit);
        }
    }

    public static void registerKit(final FFAKit kit){
        Preconditions.checkNotNull(kit, "kit");
        Preconditions.checkArgument(!getKit(kit.getName()).isPresent(), "kit already defined");
        KitManager.kits.add(kit);
    }

    public static List<FFAKit> getKits(){
        return new ArrayList<>(kits);
    }

    public static void deleteKit(final FFAKit kit){

        Preconditions.checkNotNull(kit, "cannot delete an unknown kit");

        final FileConfiguration config=plugin.getConfig();
        final ConfigurationSection section=config.getConfigurationSection("kits");
        if(section==null){ return; }

        section.set(kit.getName()
                       .toLowerCase(), null);
        plugin.saveConfig();
        KitManager.kits.removeIf(other->kit.getName()
                                           .equals(other.getName()));
    }
}
