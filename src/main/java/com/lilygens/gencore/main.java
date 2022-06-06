package com.lilygens.gencore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class main extends JavaPlugin {
    public static HashMap<Material, generator> Generators = new HashMap();
    public static ArrayList<generator> ListedGenerators = new ArrayList<>();
    public static Economy economy = null;

    public main() {
    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        } else {
            RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            } else {
                economy = rsp.getProvider();
                return true;
            }
        }
    }

    public static boolean checkRemoveMoney(Player player, Double amount) {
        double balance = economy.getBalance(player);
        if (balance >= amount) {
            economy.withdrawPlayer(player, amount);
            return true;
        } else {
            return false;
        }
    }

    private void startLoop() {
        Runnable r = new dropping();
        (new Thread(r)).start();
        Runnable re = new saving();
        (new Thread(re)).start();
    }

    public static void addGenerator(String block, String drop, String next, Integer worth, Integer upgrade, String name, String lore) {
        Material b = Material.getMaterial(block.toUpperCase());
        Material d = Material.getMaterial(drop.toUpperCase());
        Material n = Material.getMaterial(next.toUpperCase());
        List<String> l = Arrays.asList(lore.replace("||", "@@").split("@@"));
        if(next.equals("MAX")) {
            n = null;
        }
        generator gen = new generator(b, name, d, worth, upgrade, l, n);
        Generators.putIfAbsent(b, gen);
        ListedGenerators.add(gen);
    }

    public static void clearData(Player player) {
        events.active_gens.remove(player);
        events.placed_gens.remove(player);
        events.slots_gens.remove(player);
    }

    public static void giveGenerator(Player player, Material type) {
        player.getInventory().addItem(Generators.get(type).getItem());
    }

    private void initItems() {
        itemeditor.createItem("close_btn1", "&c&lExit", null, Material.BARRIER, false);
        itemeditor.createItem("border1", "&b", null, Material.GRAY_STAINED_GLASS_PANE, false);
        itemeditor.createItem("border2", "&b", null, Material.BLACK_STAINED_GLASS_PANE, false);
        itemeditor.createItem("back_btn", "&b&lPrevious page", null, Material.ARROW, false);
        itemeditor.createItem("next_btn", "&b&lNext page", null, Material.ARROW, false);
    }

    private void setupGenerators() {
        try {
            Objects.requireNonNull(this.getConfig().getConfigurationSection("gens")).getKeys(false).forEach((key) -> {
                String block = String.valueOf(this.getConfig().getString("gens." + key + ".block"));
                String drop = String.valueOf(this.getConfig().getString("gens." + key + ".drop"));
                String next = String.valueOf(this.getConfig().getString("gens." + key + ".next"));
                Integer worth = Integer.valueOf(Objects.requireNonNull(this.getConfig().getString("gens." + key + ".worth")));
                Integer upgrade_price = Integer.valueOf(Objects.requireNonNull(this.getConfig().getString("gens." + key + ".upgrade_price")));
                String name = String.valueOf(this.getConfig().getString("gens." + key + ".name"));
                String lore = String.valueOf(this.getConfig().getString("gens." + key + ".lore"));
                addGenerator(block, drop, next, worth, upgrade_price, name, lore);
            });
        } catch (NullPointerException var2) {
            Bukkit.getLogger().severe("Core Loading Error!");
        }
    }

    public void onEnable() {
        pluginhandler.set(this);
        database.setupDatabase();
        if (!this.setupEconomy()) {
            Bukkit.getLogger().severe(String.format("[%s] - No Vault dependency found!", this.getDescription().getName()));
            this.getServer().getPluginManager().disablePlugin(this);
        }
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(new events(), this);
        this.getServer().getPluginManager().registerEvents(new gui(), this);
        this.setupGenerators();
        initItems();
        gui.setupGensGuis();
        this.startLoop();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("GenSlots")) {
            Player player = (Player)sender;
            if (player.hasPermission("gens.giveslots")) {
                if (args.length == 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lIncorrect Usage!"));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &8- &7/genslots (&6Player&7) (&6Number&7)"));
                    return true;
                }
                Player p = Bukkit.getPlayer(args[0]);
                if (p == null) {
                    return true;
                }
                if (args.length >= 2) {
                    Inventory inv = p.getInventory();
                    int s = inv.firstEmpty();
                    if (s != -1) {
                        ItemStack item = new ItemStack(Material.PAPER);
                        ItemMeta meta = item.getItemMeta();
                        try {
                            Objects.requireNonNull(meta).setDisplayName(ChatColor.translateAlternateColorCodes('&', "&9Gen Slots Voucher &7(Right-Click)"));
                            int num = Integer.parseInt(args[1]);
                            meta.addEnchant(Enchantment.LURE, num, true);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            List<String> lore = new ArrayList();
                            lore.add(ChatColor.translateAlternateColorCodes('&', "&9Slots: &f" + num));
                            lore.add(ChatColor.translateAlternateColorCodes('&', "&9Giver: &f" + p.getName()));
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                            inv.setItem(s, item);
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully given a GenSlot Voucher to &6" + p.getDisplayName()));
                            return true;
                        } catch (NumberFormatException var13) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lIncorrect Usage!"));
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &8- &7/genslots (&6Player&7) (&6Number&7)"));
                            return true;
                        }
                    }

                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis player's inventory is full!"));
                    return true;
                }

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lIncorrect Usage!"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &8- &7/genslots (&6Player&7) (&6Number&7)"));
                return true;
            }
        } else if (label.equalsIgnoreCase("Genlist") || label.equalsIgnoreCase("GensList") || label.equalsIgnoreCase("GenLists") || label.equalsIgnoreCase("GensLists")) {
            if(sender instanceof Player) {
                gui.openGensGui((Player) sender);
            } else {
                sender.sendMessage("Please execute this command as a player!");
            }
        }
        return false;
    }

    public void onDisable() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            database.savePlayerData(player);
        }
    }
}
