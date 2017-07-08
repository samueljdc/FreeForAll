package me.angrypostman.freeforall.kit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class FFAKit {

    private String name = null;
    private String permission = null;

    private ItemStack helmet = null;
    private ItemStack chestplate = null;
    private ItemStack leggings = null;
    private ItemStack boots = null;

    private boolean isDefault = false;

    private List<ItemStack> inventoryItems = null;

    public FFAKit(String name, String permission) {
        this.name = name;
        this.permission = permission;
    }

    public String getName() {
        return name;
    }

    public boolean hasPermission() {
        return getPermission() != null && !getPermission().isEmpty();
    }

    public String getPermission() {
        return permission;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public void setHelmet(Material helmet) {
        this.helmet = new ItemStack(helmet);
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }

    public ItemStack getChestplate() {
        return chestplate;
    }

    public void setChestplate(Material chestplate) {
        this.chestplate = new ItemStack(chestplate);
    }

    public void setChestplate(ItemStack chestplate) {
        this.chestplate = chestplate;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public void setLeggings(Material leggings) {
        this.leggings = new ItemStack(leggings);
    }

    public void setLeggings(ItemStack leggings) {
        this.leggings = leggings;
    }

    public ItemStack getBoots() {
        return boots;
    }

    public void setBoots(Material boots) {
        this.boots = new ItemStack(boots);
    }

    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }

    public List<ItemStack> getInventoryItems() {
        return inventoryItems;
    }

    public void setInventoryItems(List<ItemStack> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
