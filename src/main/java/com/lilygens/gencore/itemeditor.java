package com.lilygens.gencore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;

public class itemeditor {
    public static HashMap<String, ItemStack> ItemStacks = new HashMap();

    public itemeditor() {
    }

    public static void createItem(String name, String display, @Nullable String lore, Material material, Boolean glowing) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', display));
        if(lore != null) {
            List<String> lore2 = Arrays.asList(lore.split("::"));
            List<String> lore3 = new ArrayList<>();
            lore2.forEach(i -> lore3.add(ChatColor.translateAlternateColorCodes('&', i)));
            meta.setLore(lore3);
        }
        if (glowing) {
            meta.addEnchant(Enchantment.LUCK, 1, true);
        }
        item.setItemMeta(meta);
        ItemStacks.put(name, item);
    }

    public static ItemStack getItem(String name) {
        return ItemStacks.get(name);
    }
}
