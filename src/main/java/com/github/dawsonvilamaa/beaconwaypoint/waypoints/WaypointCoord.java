package com.github.dawsonvilamaa.beaconwaypoint.waypoints;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.simple.JSONObject;

import java.util.Objects;

public class WaypointCoord {
    private int x;
    private int y;
    private int z;
    private String worldName;
    private int hashCode;

    /**
     * @param x
     * @param y
     * @param z
     * @param worldName
     */
    public WaypointCoord(int x, int y, int z, String worldName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
        this.hashCode = Objects.hash(x, y, z, worldName);
    }

    public WaypointCoord(JSONObject jsonObject) {
        this.x = Integer.parseInt(jsonObject.get("x").toString());
        this.y = Integer.parseInt(jsonObject.get("y").toString());
        this.z = Integer.parseInt(jsonObject.get("z").toString());
        this.worldName = jsonObject.get("world").toString();
        this.hashCode = Objects.hash(x, y, z, worldName);
    }

    public WaypointCoord(Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.worldName = location.getWorld().getName();
        this.hashCode = Objects.hash(x, y, z, worldName);
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

    /**
     * @return worldName
     */
    public String getWorldName() {
        return this.worldName;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WaypointCoord) {
            WaypointCoord other = (WaypointCoord) obj;
            return this.worldName.equals(((WaypointCoord) obj).getWorldName()) && this.x == other.getX() && this.y == other.getY() && this.z == other.getZ() && this.worldName.equals(other.getWorldName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
