package me.angrypostman.freeforall.kit;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class FFAKit{

    public FFAKit(final String name){
        this(name, null);
    }

    public FFAKit(final String name,
                  final String permission){
        this.name=name;
        this.permission=permission;
    }

    public String getName(){
        return this.name;
    }

    public boolean hasPermission(){
        return getPermission()!=null&&!getPermission().isEmpty();
    }

    public String getPermission(){
        return this.permission;
    }

    public void setPermission(final String permission){
        this.permission=permission;
    }

    public ItemStack getHelmet(){
        return this.helmet;
    }

    public void setHelmet(final ItemStack helmet){
        this.helmet=helmet;
    }

    public void setHelmet(final Material helmet){
        this.helmet=new ItemStack(helmet);
    }

    public ItemStack getChestplate(){
        return this.chestplate;
    }

    public void setChestplate(final ItemStack chestplate){
        this.chestplate=chestplate;
    }

    public void setChestplate(final Material chestplate){
        this.chestplate=new ItemStack(chestplate);
    }

    public ItemStack getLeggings(){
        return this.leggings;
    }

    public void setLeggings(final ItemStack leggings){
        this.leggings=leggings;
    }

    public void setLeggings(final Material leggings){
        this.leggings=new ItemStack(leggings);
    }

    public ItemStack getBoots(){
        return this.boots;
    }

    public void setBoots(final ItemStack boots){
        this.boots=boots;
    }

    public void setBoots(final Material boots){
        this.boots=new ItemStack(boots);
    }

    public List<ItemStack> getInventoryItems(){
        return this.inventoryItems;
    }

    public void setInventoryItems(final List<ItemStack> inventoryItems){
        this.inventoryItems=inventoryItems;
    }

    public List<PotionEffect> getPotionEffects(){
        return this.potionEffects;
    }

    public void setPotionEffects(final List<PotionEffect> potionEffects){
        this.potionEffects=potionEffects;
    }

    public boolean isDefault(){
        return this.isDefault;
    }

    public void setDefault(final boolean isDefault){
        this.isDefault=isDefault;
    }

    private String name=null;
    private String permission=null;
    private ItemStack helmet=null;
    private ItemStack chestplate=null;
    private ItemStack leggings=null;
    private ItemStack boots=null;
    private boolean isDefault=false;
    private List<ItemStack> inventoryItems=null;
    private List<PotionEffect> potionEffects=null;
}
