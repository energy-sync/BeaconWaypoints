package com.github.dawsonvilamaa.beaconwaypoint;

import com.github.dawsonvilamaa.beaconwaypoint.gui.MenuManager;
import com.github.dawsonvilamaa.beaconwaypoint.listeners.InventoryListener;
import com.github.dawsonvilamaa.beaconwaypoint.listeners.WorldListener;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.Waypoint;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointCoord;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointManager;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointPlayer;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class Main extends JavaPlugin {
    public static Main plugin;
    public static WaypointManager waypointManager;
    public static MenuManager menuManager;

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

        //bStats
        Metrics metrics = new Metrics(this, 14276);

        //register commands
        BWCommandExecutor commandExecutor = new BWCommandExecutor(this);
        Objects.requireNonNull(getCommand("waypoint")).setExecutor(commandExecutor);

        //register events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(worldListener, this);
        pm.registerEvents(inventoryListener, this);

        //create data folder if it doesn't exist
        File pluginDir = new File("plugins/" + File.separator + "BeaconWaypoints");
        if (!pluginDir.exists()) pluginDir.mkdir();

        //create folder for player waypoints if it doesn't exist
        File playerDir = new File("plugins/" + File.separator + "BeaconWaypoints/" + File.separator + "players");
        if (!playerDir.exists()) playerDir.mkdir();

        loadData();
        autoSave.runTaskTimer(plugin, 6000, 6000);

        //update checker
        new UpdateChecker(this, 99866).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version))
                this.getLogger().info("\n=======================================================================\n"
                        + ChatColor.AQUA + "A new version of Beacon Waypoints is available!\n"
                        + ChatColor.YELLOW + "Current version: " + Main.plugin.getDescription().getVersion()
                        + "\nUpdated version: " + version
                        + ChatColor.WHITE + "\nDownload link: " + ChatColor.UNDERLINE + "https://www.spigotmc.org/resources/beaconwaypoints.99866\n"
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
}