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
    public static ArrayList<Inventory> GensList = new ArrayList<>();
    public static Inventory GenList;
    public static boolean pagination = true;
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
        int pages = (int) Math.floor(main.Generators.size() / 21f)+1;

        for(int page = 1; page < pages+1; page++) {
            int start = (page*21)-20;
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
            if(page >= 2) {
                inv.setItem(37, itemeditor.getItem("back_btn"));
            }
            if(page < pages) {
                inv.setItem(43, itemeditor.getItem("next_btn"));
            }
            final int[] loops = {0};
            final int[] slot = {10};
            main.ListedGenerators.forEach(value -> {
                if(slot[0] < 35){
                    loops[0]++;
                    if(loops[0] >= start){
                        if(slot[0] == 17 || slot[0] == 26){
                            slot[0]+=2;
                        }
                        inv.setItem(slot[0], value.getItem());
                        slot[0]++;
                    }
                }
            });
            if(pages == 1) {
                pagination= false;
            }
            GensList.add(inv);
        }

    }

    public static void openGensGui(Player player) {
        player.openInventory(GensList.get(0));
    }

    @EventHandler
    public void guiClickEvent(InventoryClickEvent event) {
        if(!pagination) {
            if (event.getInventory().equals(GensList.get(0))) {
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
                    case 43:
                        if(page != GensList.size()-1) {
                            player.openInventory(GensList.get(page+1));
                        }
                        break;
                    case 37:
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
