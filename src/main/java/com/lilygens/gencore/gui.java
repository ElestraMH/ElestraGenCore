package com.lilygens.gencore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public class gui implements Listener {
    public static ArrayList<Inventory> GensList;
    public static Inventory GenList;
    public static boolean pagination;
    public static ArrayList<Integer> slots = new ArrayList<>();

    public static void setupGensGuis() {
        for (int i = 10; i <= 16; ++i) {
            slots.add(i);
        }
        for (int i = 19; i <= 25; ++i) {
            slots.add(i);
        }
        for (int i = 28; i <= 34; ++i) {
            slots.add(i);
        }
        int pages = main.Generators.size() / 27;
        if (pages == 0) {
            pagination = false;
            Inventory inv = Bukkit.createInventory(null, 45, ChatColor.translateAlternateColorCodes('&', "&8Gen List"));
            for (int i = 0; i <= 9; ++i) {
                inv.setItem(i, itemeditor.getItem("border2"));
            }
            for (int i = 35; i <= 44; ++i) {
                inv.setItem(i, itemeditor.getItem("border2"));
            }
            inv.setItem(slots.get(0), itemeditor.getItem("diamond"));
            inv.setItem(17, itemeditor.getItem("border2"));
            inv.setItem(18, itemeditor.getItem("border2"));
            inv.setItem(26, itemeditor.getItem("border2"));
            inv.setItem(27, itemeditor.getItem("border2"));
            inv.setItem(40, itemeditor.getItem("close_btn1"));
            final int[] i = {0};
            main.ListedGenerators.forEach(value -> {
                inv.setItem(slots.get(i[0]), value.getItem());
                i[0]++;
            });
            GenList = inv;
        } else {
            pagination = true;
        }
    }

    public static void openGensGui(Player player) {
        if(!pagination) {
            player.openInventory(GenList);
        } else {
            player.openInventory(GensList.get(0));
        }
    }

    @EventHandler
    public void guiClickEvent(InventoryClickEvent event) {
        if(!pagination) {
            if (event.getInventory().equals(GenList)) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                if (event.getSlot() == 40) {
                    player.closeInventory();
                }
                if(player.hasPermission("gens.givegen")) {
                    if(slots.contains(event.getSlot())) {
                        player.getInventory().addItem(event.getInventory().getItem(event.getSlot()));
                    }
                }
            }
        } else {
            if(GensList.contains(event.getInventory())) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                int page = GensList.indexOf(event.getInventory());
                switch (event.getSlot()) {
                    case 40:
                        player.closeInventory();
                        break;
                    case 44:
                        if(page != GensList.size()-1) {
                            player.openInventory(GensList.get(page+1));
                        }
                        break;
                    case 36:
                        if(page > 0) {
                            player.openInventory(GensList.get(page-1));
                        }
                        break;
                }
                if(player.hasPermission("gens.givegen")) {
                    if(slots.contains(event.getSlot())) {
                        player.getInventory().addItem(event.getInventory().getItem(event.getSlot()));
                    }
                }
            }
        }
    }
}
