package com.github.dawsonvilamaa.beaconwaypoint;

import com.github.dawsonvilamaa.beaconwaypoint.gui.MenuManager;
import com.github.dawsonvilamaa.beaconwaypoint.listeners.InventoryListener;
import com.github.dawsonvilamaa.beaconwaypoint.listeners.WorldListener;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.Waypoint;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointCoord;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointManager;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointPlayer;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
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

    private final WorldListener worldListener = new WorldListener(this);
    private final InventoryListener inventoryListener = new InventoryListener(this);

    @Override
    public void onEnable() {
        plugin = this;
        waypointManager = new WaypointManager();
        menuManager = new MenuManager();

        //register commands
        BWCommandExecutor commandExecutor = new BWCommandExecutor(this);
        getCommand("waypoint").setExecutor(commandExecutor);

        //register events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(worldListener, this);
        pm.registerEvents(inventoryListener, this);

        //create data folder if it doesn't exist
        File pluginDir = new File("plugins\\BeaconWaypoints");
        if (!pluginDir.exists()) pluginDir.mkdir();

        //create folder for player waypoints if it doesn't exist
        File playerDir = new File("plugins\\BeaconWaypoints\\players");
        if (!playerDir.exists()) playerDir.mkdir();

        //read data from public file
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonWaypoints = (JSONArray) parser.parse(new FileReader("plugins\\BeaconWaypoints\\public.json"));
            for (JSONObject jsonWaypoint : (Iterable<JSONObject>) jsonWaypoints)
                waypointManager.addPublicWaypoint(new Waypoint(jsonWaypoint));
        } catch(IOException | ParseException e) {
            getLogger().info(e.getMessage());
        }

        //read data from player files
        try {
            for (File playerFile : Objects.requireNonNull(playerDir.listFiles())) {
                if (playerFile.isFile() && playerFile.getName().endsWith(".json")) {
                    JSONObject jsonPlayer = (JSONObject) parser.parse(new FileReader("plugins\\BeaconWaypoints\\players\\" + playerFile.getName()));
                    waypointManager.addPlayer(UUID.fromString(jsonPlayer.get("uuid").toString()));
                    for (JSONObject jsonWaypoint : (Iterable<JSONObject>) jsonPlayer.get("waypoints"))
                        waypointManager.addPrivateWaypoint(UUID.fromString(jsonPlayer.get("uuid").toString()), new Waypoint(jsonWaypoint));
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        //save public waypoints
        JSONArray jsonWaypoints = new JSONArray();
        for (Waypoint waypoint : waypointManager.getPublicWaypoints().values())
            if (waypoint != null) jsonWaypoints.add(waypoint.toJSON());

        FileWriter waypointFile = null;
        try {
            waypointFile = new FileWriter("plugins\\BeaconWaypoints\\public.json");
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
                playerWaypointFile = new FileWriter("plugins\\BeaconWaypoints\\players\\" + waypointPlayer.getUUID().toString() + ".json");
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
    }

    /**
     * Returns a waypoint from a waypoint coordinate, public or private, or null if it doesn't exist
     * @param coord
     * @return waypoint
     */
    public Waypoint getWaypoint(WaypointCoord coord) {
        Waypoint waypoint = waypointManager.getPublicWaypoints().get(coord);

        if (waypoint == null) {
            for (WaypointPlayer waypointPlayer : waypointManager.getWaypointPlayers().values()) {
                waypoint = waypointPlayer.getWaypoints().get(coord);
                if (waypoint != null) break;
            }
        }

        return waypoint;
    }
}