package com.github.dawsonvilamaa.beaconwaypoint.waypoints;

import org.bukkit.Location;
import org.bukkit.Material;
import org.json.simple.JSONObject;

public class Waypoint {
    private String name;
    private WaypointCoord coord;
    private Material icon;

    /**
     * @param name
     * @param location
     */
    public Waypoint(String name, Location location) {
        this.name = name;
        this.coord = new WaypointCoord(location);
        this.icon = Material.BEACON;
    }

    public Waypoint(JSONObject jsonWaypoint) {
        this.name = jsonWaypoint.get("name").toString();
        int x = Integer.parseInt(jsonWaypoint.get("x").toString());
        int y = Integer.parseInt(jsonWaypoint.get("y").toString());
        int z = Integer.parseInt(jsonWaypoint.get("z").toString());
        String worldName = jsonWaypoint.get("world").toString();
        this.coord = new WaypointCoord(x, y, z, worldName);
        this.icon = Material.valueOf(jsonWaypoint.get("icon").toString());
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
        return this.coord.getX();
    }

    /**
     * @return y
     */
    public int getY() {
        return this.coord.getY();
    }

    /**
     * @return z
     */
    public int getZ() {
        return this.coord.getZ();
    }

    /**
     * @return worldName
     */
    public String getWorldName() {
        return this.coord.getWorldName();
    }

    /**
     * @return coord
     */
    public WaypointCoord getCoord() {
        return this.coord;
    }

    /**
     * @return material
     */
    public Material getIcon() {
        return this.icon;
    }

    /**
     * @param material
     */
    public void setIcon(Material material) {
        Material newMaterial = Material.getMaterial(material.toString());
        if (newMaterial != null)
            this.icon = material;
    }

    /**
     * @return jsonWaypoint
     */
    public JSONObject toJSON() {
        JSONObject jsonWaypoint = new JSONObject();
        jsonWaypoint.put("name", this.name);
        jsonWaypoint.put("x", String.valueOf(this.coord.getX()));
        jsonWaypoint.put("y", String.valueOf(this.coord.getY()));
        jsonWaypoint.put("z", String.valueOf(this.coord.getZ()));
        jsonWaypoint.put("world", this.coord.getWorldName());
        jsonWaypoint.put("icon", this.icon.toString());
        return jsonWaypoint;
    }
}