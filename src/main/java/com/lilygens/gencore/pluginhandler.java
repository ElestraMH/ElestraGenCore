package com.lilygens.gencore;

import org.bukkit.plugin.Plugin;

public class pluginhandler {
    private static Plugin plugin = null;

    public pluginhandler() {
    }

    public static void set(Plugin a) {
        plugin = a;
    }

    public static Plugin get() {
        return plugin;
    }
}
