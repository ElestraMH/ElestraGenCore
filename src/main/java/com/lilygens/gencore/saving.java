package com.lilygens.gencore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class saving implements Runnable {
    Runnable save = () -> {
        Bukkit.broadcastMessage(ChatColor.GREEN + "Saving data...");
        for (Player p : Bukkit.getOnlinePlayers()) {
            database.savePlayerData(p);
        }
        Bukkit.broadcastMessage(ChatColor.GREEN + "Data saved!");
    };

    public saving() {
    }

    public void run() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(this.save, 0L, 5L, TimeUnit.MINUTES);
    }
}
