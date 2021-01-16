package com.github.dawsonvilamaa.beaconwaypoint;

import org.bukkit.Location;
import org.json.simple.JSONObject;

public class Waypoint {
    private String name;
    private int x;
    private int y;
    private int z;

    /**
     * @param name
     * @param location
     */
    public Waypoint(String name, Location location) {
        this.name = name;
        this.x = (int) location.getBlockX();
        this.y = (int) location.getBlockY();
        this.z = (int) location.getBlockZ();
    }

    public Waypoint(JSONObject jsonWaypoint) {
        this.name = jsonWaypoint.get("name").toString();
        this.x = Integer.parseInt(jsonWaypoint.get("x").toString());
        this.y = Integer.parseInt(jsonWaypoint.get("y").toString());
        this.z = Integer.parseInt(jsonWaypoint.get("z").toString());
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return x
     */
    public int getX() {
        return this.x;
    }

    /**
     * @return y
     */
    public int getY() {
        return this.y;
    }

    /**
     * @return z
     */
    public int getZ() {
        return this.z;
    }

    public JSONObject toJSON() {
        JSONObject jsonWaypoint = new JSONObject();
        jsonWaypoint.put("name", this.name);
        jsonWaypoint.put("x", String.valueOf(this.x));
        jsonWaypoint.put("y", String.valueOf(this.y));
        jsonWaypoint.put("z", String.valueOf(this.z));
        return jsonWaypoint;
    }
}