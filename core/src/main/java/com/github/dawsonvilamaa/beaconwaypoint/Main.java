package com.github.dawsonvilamaa.beaconwaypoint;

import com.github.dawsonvilamaa.beaconwaypoint.gui.MenuManager;
import com.github.dawsonvilamaa.beaconwaypoint.listeners.InventoryListener;
import com.github.dawsonvilamaa.beaconwaypoint.listeners.WorldListener;
import com.github.dawsonvilamaa.beaconwaypoint.version.VersionMatcher;
import com.github.dawsonvilamaa.beaconwaypoint.version.VersionWrapper;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.Waypoint;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointManager;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;

public class Main extends JavaPlugin {
    public static Main plugin;
    private static YamlConfiguration languageManager;
    public static WaypointManager waypointManager;
    public static MenuManager menuManager;
    public static VersionWrapper version;

    private final WorldListener worldListener = new WorldListener();
    private final InventoryListener inventoryListener = new InventoryListener(this);

    private BukkitRunnable autoSave = new BukkitRunnable() {
        @Override
        public void run() {
            saveData();
        }
    };

    @Override
    public void onEnable() {
        plugin = this;
        waypointManager = new WaypointManager();
        menuManager = new MenuManager();

        //get version wrapper
        version = new VersionMatcher().match();

        //bStats
        Metrics metrics = new Metrics(this, 14276);
        metrics.addCustomChart(new Metrics.SingleLineChart("waypoints", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return waypointManager.getPublicWaypoints().size() + waypointManager.getNumPrivateWaypoints();
            }
        }));

        //register commands
        BWCommandExecutor commandExecutor = new BWCommandExecutor(this);
        Objects.requireNonNull(getCommand("waypoint")).setExecutor(commandExecutor);

        //register events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(worldListener, this);
        pm.registerEvents(inventoryListener, this);

        //create data folder if it doesn't exist
        if (!getDataFolder().exists())
            getDataFolder().mkdirs();

        //create config file if it doesn't exist
        if (!new File(getDataFolder(), "config.yml").exists())
            saveDefaultConfig();

        //create folder for player waypoints if it doesn't exist
        File playerDir = new File(getDataFolder() + File.separator + "players");
        if (!playerDir.exists())
            playerDir.mkdirs();

        //load language config
        loadLanguage();

        loadData();
        autoSave.runTaskTimer(plugin, 6000, 6000);

        //update checker
        new UpdateChecker(this, 99866).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version))
                this.getLogger().info("\n=======================================================================\n"
                        + ChatColor.AQUA + languageManager.getString("new-version-available") + "\n"
                        + ChatColor.YELLOW + languageManager.getString("current-version") + ": " + Main.plugin.getDescription().getVersion() + "\n"
                        + languageManager.getString("updated-version") + ": " + version + "\n"
                        + ChatColor.WHITE +languageManager.getString("download-link") + ": " + ChatColor.UNDERLINE + "https://www.spigotmc.org/resources/beaconwaypoints.99866\n"
                        + ChatColor.RESET + "=======================================================================");
        });
    }

    @Override
    public void onDisable() {
        autoSave.cancel();
        saveData();
    }

    public void loadData() {
        this.reloadConfig();

        //read data from public file
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonWaypoints = (JSONArray) parser.parse(new FileReader("plugins/" + File.separator + "BeaconWaypoints/" + File.separator + "public.json"));
            for (JSONObject jsonWaypoint : (Iterable<JSONObject>) jsonWaypoints)
                waypointManager.addPublicWaypoint(new Waypoint(jsonWaypoint));
        } catch(IOException | ParseException e) {
            getLogger().info(e.getMessage());
        }

        //read data from player files
        try {
            File playerDir = new File("plugins/" + File.separator + "BeaconWaypoints/" + File.separator + "players");
            for (File playerFile : Objects.requireNonNull(playerDir.listFiles())) {
                if (playerFile.isFile() && playerFile.getName().endsWith(".json")) {
                    JSONObject jsonPlayer = (JSONObject) parser.parse(new FileReader("plugins/" + File.separator + "BeaconWaypoints/" + File.separator + "players/" + File.separator + "" + playerFile.getName()));
                    waypointManager.addPlayer(UUID.fromString(jsonPlayer.get("uuid").toString()));
                    for (JSONObject jsonWaypoint : (Iterable<JSONObject>) jsonPlayer.get("waypoints"))
                        waypointManager.addPrivateWaypoint(UUID.fromString(jsonPlayer.get("uuid").toString()), new Waypoint(jsonWaypoint));
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        //load inactive waypoints
        try {
            JSONArray jsonInactiveWaypoints = (JSONArray) parser.parse(new FileReader("plugins/" + File.separator + "BeaconWaypoints/" + File.separator + "inactive.json"));
            for (JSONObject jsonWaypoint : (Iterable<JSONObject>) jsonInactiveWaypoints)
                waypointManager.addInactiveWaypoint(new Waypoint(jsonWaypoint));
        } catch(IOException | ParseException e) {
            getLogger().info(e.getMessage());
        }
    }

    public void saveData() {
        //save public waypoints
        JSONArray jsonWaypoints = new JSONArray();
        for (Waypoint waypoint : waypointManager.getPublicWaypoints().values())
            if (waypoint != null) jsonWaypoints.add(waypoint.toJSON());

        FileWriter waypointFile = null;
        try {
            waypointFile = new FileWriter("plugins/" + File.separator + "BeaconWaypoints/" + File.separator + "public.json");
            waypointFile.write(jsonWaypoints.toJSONString());
        } catch(IOException e) {
            getLogger().info(e.getMessage());
        } finally {
            try {
                Objects.requireNonNull(waypointFile).flush();
                waypointFile.close();
            } catch (IOException ex) {
                getLogger().info(ex.getMessage());
            }
        }

        //save player waypoints
        for (WaypointPlayer waypointPlayer : waypointManager.getWaypointPlayers().values()) {
            JSONObject playerData = new JSONObject();
            playerData.put("uuid", waypointPlayer.getUUID().toString());

            JSONArray jsonPlayerWaypoints = new JSONArray();
            for (Waypoint waypoint : waypointPlayer.getWaypoints().values())
                if (waypoint != null)
                    jsonPlayerWaypoints.add(waypoint.toJSON());
            playerData.put("waypoints", jsonPlayerWaypoints);

            FileWriter playerWaypointFile = null;
            try {
                playerWaypointFile = new FileWriter("plugins/" + File.separator + "BeaconWaypoints/" + File.separator + "players/" + File.separator + "" + waypointPlayer.getUUID().toString() + ".json");
                playerWaypointFile.write(playerData.toJSONString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    Objects.requireNonNull(playerWaypointFile).flush();
                    playerWaypointFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //save inactive waypoints
        JSONArray jsonInactiveWaypoints = new JSONArray();
        for (Waypoint waypoint : waypointManager.getInactiveWaypoints().values())
            if (waypoint != null) jsonInactiveWaypoints.add(waypoint.toJSON());

        FileWriter inactiveWaypointFile = null;
        try {
            inactiveWaypointFile = new FileWriter("plugins/" + File.separator + "BeaconWaypoints/" + File.separator + "inactive.json");
            inactiveWaypointFile.write(jsonInactiveWaypoints.toJSONString());
        } catch(IOException e) {
            getLogger().info(e.getMessage());
        } finally {
            try {
                Objects.requireNonNull(inactiveWaypointFile).flush();
                inactiveWaypointFile.close();
            } catch (IOException ex) {
                getLogger().info(ex.getMessage());
            }
        }
    }

    public void loadLanguage() {
        File languageConfigFile = new File(getDataFolder(), "language.yml");
        YamlConfiguration languageConfig = YamlConfiguration.loadConfiguration(languageConfigFile);
        Reader configStream = new InputStreamReader(getResource("language.yml"), StandardCharsets.UTF_8);
        YamlConfiguration defaultLanguageConfig = YamlConfiguration.loadConfiguration(configStream);
        languageConfig.setDefaults(defaultLanguageConfig);
        languageManager = defaultLanguageConfig;

        if (!new File(getDataFolder(), "language.yml").exists()) {
            try {
                defaultLanguageConfig.save(languageConfigFile);
            } catch (IOException e) {
                Bukkit.getLogger().severe(languageConfig.getString("cannot-save-default-language-config"));
                throw new RuntimeException(e);
            }
        }
        else
            languageManager = languageConfig;
    }

    /**
     * @return languageManager
     */
    public YamlConfiguration getLanguageManager() {
        return languageManager;
    }
}