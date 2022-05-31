package com.lilygens.gencore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class dropping implements Runnable {
    Runnable dropItems = () -> {
        for (Player p : Bukkit.getOnlinePlayers()) {
            HashMap<Material, ArrayList<Location>> a = events.active_gens.get(p);
            if (a == null) {
                return;
            }
            a.forEach((key, value) -> {
                int size = value.size();
                if (size >= 1) {
                    Location c = value.get(size - 1);
                    if (!c.getChunk().isLoaded()) {
                        return;
                    }
                    ItemStack i = new ItemStack(main.Generators.get(key).getDrop(), size);
                    Location b = new Location(c.getWorld(), c.getX() + 0.5, c.getY() + 1.0, c.getZ() + 0.5);
                    Bukkit.getScheduler().runTask(pluginhandler.get(), () -> {
                        Entity e = c.getWorld().dropItem(b, i);
                        e.setVelocity(e.getVelocity().zero());
                    });
                }
            });
        }
    };

    public dropping() {
    }

    public void run() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(this.dropItems, 0L, 10L, TimeUnit.SECONDS);
    }
}
