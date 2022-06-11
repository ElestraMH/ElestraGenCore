package com.lilygens.gencore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class events implements Listener {
    public static HashMap<Player, Integer> slots_gens = new HashMap();
    public static HashMap<Player, Integer> placed_gens = new HashMap();
    public static HashMap<Player, HashMap<Material, ArrayList<Location>>> active_gens = new HashMap();
    private static final HashMap<Integer, Location> gcreateloc = new HashMap();

    public events() {
    }

    public static void FillBlocks(Location l1, Location l2, Material material, Player player) {
        double minX = Math.min(l1.getX(), l2.getX());
        double minY = Math.min(l1.getY(), l2.getY());
        double minZ = Math.min(l1.getZ(), l2.getZ());
        double maxX = Math.max(l1.getX(), l2.getX());
        double maxY = Math.max(l1.getY(), l2.getY());
        double maxZ = Math.max(l1.getZ(), l2.getZ());
        double total = (maxX - minX) * (maxY - minY) * (maxZ - minZ);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9&lServer: &fCurrently creating &9" + total + " &fgenerators"));
        Integer slots = getSlots(player);
        Integer placed = getPlaced(player);

        for(double y = minY; y <= maxY; ++y) {
            for(double z = minZ; z <= maxZ; ++z) {
                for(double x = minX; x <= maxX; ++x) {
                    World world = Bukkit.getWorld("world");
                    Block block = world.getBlockAt((int)x, (int)y, (int)z);
                    block.setType(material);
                    active_gens.putIfAbsent(player, new HashMap());
                    HashMap<Material, ArrayList<Location>> a = active_gens.get(player);
                    ArrayList<Location> b = a.get(block.getType());
                    placed = placed + 1;
                    if (b == null) {
                        b = new ArrayList();
                    }

                    a.putIfAbsent(block.getType(), b);
                    b.add(block.getLocation());
                    a.putIfAbsent(block.getType(), b);
                    active_gens.replace(player, a);
                    placed_gens.replace(player, placed);
                }

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&aPlaced Generator &7(&f" + placed + "&7/&f" + slots + "&7)")));
            }
        }

    }

    public static String getJson(Player player) {
        HashMap<Material, ArrayList<Location>> map = new HashMap();
        active_gens.putIfAbsent(player, map);
        map = active_gens.get(player);
        JSONObject json = new JSONObject();
        Iterator var3 = map.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<Material, ArrayList<Location>> entry = (Map.Entry)var3.next();
            String key = String.valueOf(entry.getKey());
            Enumeration<Location> e = Collections.enumeration((Collection)entry.getValue());
            ArrayList<String> loc = new ArrayList();

            while(e.hasMoreElements()) {
                loc.add(String.valueOf(e.nextElement()));
            }

            json.put(key, loc);
        }

        return json.toString();
    }

    public static HashMap<Material, ArrayList<Location>> getData(String str) throws ParseException {
        HashMap<Material, ArrayList<Location>> map = new HashMap();
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(str);
        JSONObject jsonObject = (JSONObject)obj;
        Iterator var5 = jsonObject.keySet().iterator();

        while(var5.hasNext()) {
            Object o = var5.next();
            String key = (String)o;
            JSONArray list = (JSONArray)jsonObject.get(key);
            ArrayList<Location> locs = new ArrayList();
            Iterator var10 = list.iterator();
            World world = Bukkit.getWorld("plots");
            while(var10.hasNext()) {
                Object loc = var10.next();
                String a = String.valueOf(loc).replace("=", "/").replace(",", "/");
                a = StringUtils.substring(a, 0, a.length() - 1);
                String[] arr = a.split("/");
                Location l = new Location(world, Double.parseDouble(arr[4]), Double.parseDouble(arr[6]), Double.parseDouble(arr[8]), Float.parseFloat(arr[10]), Float.parseFloat(arr[12]));
                locs.add(l);
            }

            map.put(Material.getMaterial(key), locs);
        }

        return map;
    }

    public static Integer getSlots(Player player) {
        slots_gens.putIfAbsent(player, 50);
        return slots_gens.get(player);
    }

    public static Integer getPlaced(Player player) {
        placed_gens.putIfAbsent(player, 0);
        return placed_gens.get(player);
    }

    @EventHandler
    public static void onJoin(PlayerJoinEvent event) throws ParseException {
        Player player = event.getPlayer();
        String[] query = database.queryPlayer(player);
        if (query == null) {
            database.savePlayerData(player);
            query = database.queryPlayer(player);
        }

        if (query != null) {
            slots_gens.remove(player);
            slots_gens.put(player, Integer.valueOf(query[1]));
            placed_gens.remove(player);
            placed_gens.put(player, Integer.valueOf(query[2]));
            if (active_gens.get(player) == null) {
                active_gens.put(player, getData(query[3]));
            }
        }

    }

    @EventHandler
    public static void onQuit(PlayerQuitEvent event) {
        database.savePlayerData(event.getPlayer());
    }

    @EventHandler
    public static void GenInteract(PlayerInteractEvent event) {
        ItemStack i;
        if (event.getAction().toString().equals("LEFT_CLICK_BLOCK")) {
            try {
                Material block = event.getClickedBlock().getType();
                if (main.Generators.containsKey(block)) {
                    HashMap<Material, ArrayList<Location>> a = active_gens.get(event.getPlayer());
                    if (a == null) {
                        event.setCancelled(true);
                        event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&cThis is not your generator")));
                        return;
                    }
                    ArrayList<Location> b = a.get(block);
                    if (b != null && b.contains(event.getClickedBlock().getLocation())) {
                        event.setCancelled(true);
                        event.getClickedBlock().setType(Material.AIR);
                        placed_gens.replace(event.getPlayer(), getPlaced(event.getPlayer()) - 1);
                        main.giveGenerator(event.getPlayer(), block);
                        b.remove(event.getClickedBlock().getLocation());
                        event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&aPicked up generator")));
                    } else {
                        event.setCancelled(true);
                        event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&cThis is not your generator")));
                    }
                } else {
                    if (event.getHand().equals(EquipmentSlot.OFF_HAND)) {
                        return;
                    }

                    i = event.getPlayer().getInventory().getItemInMainHand();
                    if (i.getType().equals(Material.CARROT_ON_A_STICK)) {
                        if (!i.getItemMeta().hasDisplayName()) {
                            return;
                        }

                        if (i.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&c&lGenCreator"))) {
                            gcreateloc.put(1, event.getClickedBlock().getLocation());
                            event.setCancelled(true);
                        }
                    }
                }
            } catch (NullPointerException var14) {
                event.setCancelled(true);
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            if (event.getHand().equals(EquipmentSlot.OFF_HAND)) {
                return;
            }

            i = player.getInventory().getItemInMainHand();
            if (i.getType() == Material.PAPER) {
                ItemMeta m = i.getItemMeta();
                if (m == null) {
                    return;
                }

                if (m.getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&9Gen Slots Voucher &7(Right-Click)"))) {
                    int num = m.getEnchantLevel(Enchantment.LURE);
                    if (num >= 1) {
                        num += getSlots(player);
                        slots_gens.replace(player, num);
                        i.setAmount(i.getAmount() - 1);
                    }
                }
            } else if (i.getType() == Material.LIME_DYE && i.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&a&lConfirm"))) {
                FillBlocks(gcreateloc.get(1), gcreateloc.get(2), Material.EMERALD_BLOCK, player);
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (event.getPlayer().isSneaking()) {
                    Block block = event.getClickedBlock();
                    if (main.Generators.containsKey(block.getType())) {
                        try {
                            generator gen = main.Generators.get(block.getType());
                            HashMap<Material, ArrayList<Location>> c = active_gens.get(event.getPlayer());
                            ArrayList<Location> a = c.get(block.getType());
                            if (a != null && a.contains(block.getLocation())) {
                                if(gen.getNextBlock() == null) {
                                    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&cThis Generator Is Already Max Level")));
                                    return;
                                }
                                double needed = gen.getUpgradeCost();
                                if (main.checkRemoveMoney(event.getPlayer(), needed)) {
                                    a.remove(block.getLocation());
                                    block.setType(gen.getNextBlock());
                                    c.putIfAbsent(block.getType(), new ArrayList());
                                    a = c.get(block.getType());
                                    a.add(block.getLocation());
                                    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully Upgraded Generator")));
                                } else {
                                    double left = needed - main.economy.getBalance(event.getPlayer());
                                    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&cYou need &a$" + left + " &cto upgrade this")));
                                }
                            } else {
                                event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&cThis is not your generator")));
                            }
                        } catch (NullPointerException e) {
                            Bukkit.getLogger().severe(String.valueOf(e));
                            event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&cThis is not your generator")));
                        }
                    }
                } else {
                    if (event.getHand().equals(EquipmentSlot.OFF_HAND)) {
                        return;
                    }

                    ItemStack it = player.getInventory().getItemInMainHand();
                    if (it.getType().equals(Material.CARROT_ON_A_STICK)) {
                        if (!it.getItemMeta().hasDisplayName()) {
                            return;
                        }

                        if (it.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&c&lGenCreator"))) {
                            gcreateloc.put(2, event.getClickedBlock().getLocation());
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }

    }

    @EventHandler
    public static void blockPlace(BlockPlaceEvent event) {
        if (main.Generators.containsKey(event.getBlock().getType())) {
            Player player = event.getPlayer();
            Integer slots = getSlots(player);
            Integer placed = getPlaced(player);
            placed = placed + 1;
            if (placed <= slots) {
                if (!event.isCancelled()) {
                    if(event.getBlock().getWorld() != Bukkit.getWorld("plots")) {
                        event.setCancelled(true);
                        return;
                    }
                    active_gens.putIfAbsent(player, new HashMap());
                    HashMap<Material, ArrayList<Location>> a = active_gens.get(player);
                    ArrayList<Location> b = a.get(event.getBlock().getType());
                    if (b == null) {
                        b = new ArrayList();
                    }
                    a.putIfAbsent(event.getBlock().getType(), b);
                    b.add(event.getBlock().getLocation());
                    a.putIfAbsent(event.getBlock().getType(), b);
                    active_gens.replace(player, a);
                    placed_gens.replace(player, placed);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&aPlaced Generator &7(&f" + placed + "&7/&f" + slots + "&7)")));
                }
            } else {
                event.setCancelled(true);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&cYou already have &f" + slots + "&7/&f" + slots + " &cGenerators placed")));
            }
        }

    }
}
