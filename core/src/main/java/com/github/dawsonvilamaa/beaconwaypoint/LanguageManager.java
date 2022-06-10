package com.github.dawsonvilamaa.beaconwaypoint;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;

import static java.lang.System.in;

public class LanguageManager {
    private HashMap<String, String> messages;

    public LanguageManager() {
        messages = new HashMap<>();
    }

    /*
     * @param config
     */
    public LanguageManager(YamlConfiguration config) {
        messages = new HashMap<>();
        for (String key : config.getKeys(false))
            messages.put(key, config.getString(key));
    }

    /**
     * @param key
     * @return string
     */
    public String getString(String key) {
        String string = messages.get(key);
        if (string == null)
            Bukkit.getLogger().warning("Missing language entry for \"" + key + "\"");
        return string;
    }
}