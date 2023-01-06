package com.github.dawsonvilamaa.beaconwaypoint;

import com.earth2me.essentials.IEssentials;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;

public class Main extends JavaPlugin {
    private static Main plugin;
    private static LanguageManager languageManager;
    private static WaypointManager waypointManager;
    private static MenuManager menuManager;
    private static VersionWrapper versionWrapper;

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
        versionWrapper = new VersionMatcher().match();

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

        //load language config
        loadLanguage();

        //config update checker
        try {
            ConfigUpdater.checkConfig(getConfig());
        } catch (IOException e) {
            getLogger().warning("Unable to run the update checker for config.yml");
        }

        try {
            ConfigUpdater.checkLanguageConfig(languageManager.getDefaults());
        } catch (IOException e) {
            getLogger().warning("Unable to run the update checker for language.yml");
        }

        //create folder for player waypoints if it doesn't exist
        File playerDir = new File(getDataFolder() + File.separator + "players");
        if (!playerDir.exists())
            playerDir.mkdirs();

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

        //check if EssentialsX is installed
        IEssentials essentials = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentials == null)
            this.getLogger().warning("EssentialsX is not installed, ignoring Essentials money cost for teleporting.");
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
            JSONArray jsonWaypoints = (JSONArray) parser.parse(new InputStreamReader(Files.newInputStream(Paths.get("plugins/" + File.separator + "BeaconWaypoints/" + File.separator + "public.json")), StandardCharsets.UTF_8));
            for (JSONObject jsonWaypoint : (Iterable<JSONObject>) jsonWaypoints) {
                Waypoint waypoint = new Waypoint(jsonWaypoint);
                if (waypoint.isPinned())
                    waypointManager.addPinnedWaypoint(waypoint);
                else waypointManager.addPublicWaypoint(new Waypoint(jsonWaypoint));
            }
        } catch(IOException | ParseException e) {
            getLogger().info(e.getMessage());
        }

        //read data from player files
        try {
            File playerDir = new File("plugins/" + File.separator + "BeaconWaypoints/" + File.separator + "players");
            for (File playerFile : Objects.requireNonNull(playerDir.listFiles())) {
                if (playerFile.isFile() && playerFile.getName().endsWith(".json")) {
                    JSONObject jsonPlayer = (JSONObject) parser.parse(new InputStreamReader(Files.newInputStream(Paths.get("plugins/" + File.separator + "BeaconWaypoints/" + File.separator + "players/" + File.separator + "" + playerFile.getName())), StandardCharsets.UTF_8));
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
        Collection<Waypoint> allPublicWaypoints = waypointManager.getPinnedWaypointsSortedAlphabetically();
        allPublicWaypoints.addAll(waypointManager.getPublicWaypointsSortedAlphabetically());
        for (Waypoint waypoint : allPublicWaypoints)
            if (waypoint != null) jsonWaypoints.add(waypoint.toJSON());

        Writer waypointFile = null;
        try {
            waypointFile = new OutputStreamWriter(Files.newOutputStream(Paths.get("plugins/" + File.separator + "BeaconWaypoints/" + File.separator + "public.json")), StandardCharsets.UTF_8);
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
            JSONObject playerData = waypointPlayer.toJSON();

            Writer playerWaypointFile = null;
            try {
                playerWaypointFile = new OutputStreamWriter(Files.newOutputStream(Paths.get("plugins/" + File.separator + "BeaconWaypoints/" + File.separator + "players/" + File.separator + "" + waypointPlayer.getUUID().toString() + ".json")), StandardCharsets.UTF_8);
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
        languageManager = new LanguageManager(defaultLanguageConfig);

        if (!new File(getDataFolder(), "language.yml").exists()) {
            try {
                defaultLanguageConfig.save(languageConfigFile);
            } catch (IOException e) {
                Bukkit.getLogger().severe(languageConfig.getString("cannot-save-default-language-config"));
                throw new RuntimeException(e);
            }
        }
        else
            languageManager = new LanguageManager(languageConfig);
    }

    /**
     * @return plugin
     */
    public static Main getPlugin() {
        return plugin;
    }

    /**
     * @return languageManager
     */
    public static LanguageManager getLanguageManager() {
        return languageManager;
    }

    /**
     * @return waypointManager
     */
    public static WaypointManager getWaypointManager() {
        return waypointManager;
    }

    /**
     * @return menuManager
     */
    public static MenuManager getMenuManager() {
        return menuManager;
    }

    /**
     * @return versionWrapper
     */
    public static VersionWrapper getVersionWrapper() {
        return versionWrapper;
    }
}