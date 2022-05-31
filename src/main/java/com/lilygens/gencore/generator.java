package com.lilygens.gencore;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class generator {

    private final Material drop;
    private final Integer sell;
    private final Integer upgrade;
    private final Material next;
    private final Material block;
    private final String name;
    private final ItemStack item;

    public generator(final Material block, final String name, final Material drop, final Integer sell, final Integer upgrade, final List<String> loree, @Nullable final Material next) {
        this.drop = drop;
        this.block = block;
        this.sell = sell;
        this.upgrade = upgrade;
        this.next = next;
        this.name = name;
        ItemStack item = new ItemStack(block);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> lore = new ArrayList<>();
        loree.forEach(s -> {
            lore.add(ChatColor.translateAlternateColorCodes('&', s));
        });
        meta.setLore(lore);
        item.setItemMeta(meta);
        this.item = item;
    }

    public String getName() {
        return name;
    }

    public Material getBlock() {
        return block;
    }

    public Material getDrop() {
        return drop;
    }

    public Integer getSell() {
        return sell;
    }

    public Integer getUpgradeCost() {
        return upgrade;
    }

    public Material getNextBlock() {
        return next;
    }

    public ItemStack getItem() {
        return item;
    }

}
