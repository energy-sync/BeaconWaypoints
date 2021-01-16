package com.github.dawsonvilamaa.beaconwaypoint;

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
import java.util.ArrayList;
import java.util.Iterator;

public class Main extends JavaPlugin {
    public static ArrayList<Waypoint> waypoints;

    @Override
    public void onEnable() {
        waypoints = new ArrayList<Waypoint>();

        //register commands
        BWCommandExecutor commandExecutor = new BWCommandExecutor(this);
        getCommand("waypoint").setExecutor(commandExecutor);

        //register events
        PluginManager pm = getServer().getPluginManager();

        //create data folder if it doesn't exist
        File dir = new File("plugins\\BeaconWaypoints");
        if (!dir.exists()) dir.mkdir();

        //read data
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonWaypoints = (JSONArray) parser.parse(new FileReader("plugins\\BeaconWaypoints\\waypoints.json"));
            Iterator<JSONObject> iterator = jsonWaypoints.iterator();
            while (iterator.hasNext())
                waypoints.add(new Waypoint(iterator.next()));
        } catch(IOException e) {
            getLogger().info(e.getMessage());
        } catch(ParseException ex) {
            getLogger().info(ex.getMessage());
        }
    }

    @Override
    public void onDisable() {
        //save data
        JSONArray jsonWaypoints = new JSONArray();
        for (Waypoint waypoint : waypoints)
            if (waypoint != null) jsonWaypoints.add(waypoint.toJSON());

        FileWriter waypointFile = null;
        try {
            waypointFile = new FileWriter("plugins\\BeaconWaypoints\\waypoints.json");
            waypointFile.write(jsonWaypoints.toJSONString());
        } catch(IOException e) {
            getLogger().info(e.getMessage());
        } finally {
            try {
                waypointFile.flush();
                waypointFile.close();
            } catch (IOException ex) {
                getLogger().info(ex.getMessage());
            }
        }
    }
}