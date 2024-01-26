package com.github.dawsonvilamaa.beaconwaypoint.waypoints;

import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class WaypointPlayer {
    private PlayerIdentifier identifier;
    private HashMap<WaypointCoord, Waypoint> waypoints;
    private boolean isTeleporting;

    /**
     * @param uuid
     */
    public WaypointPlayer(UUID uuid, String username) {
        this.identifier = new PlayerIdentifier(uuid, username);
        this.waypoints = new HashMap<>();
        this.isTeleporting = false;
    }

    /**
     * @param jsonPlayer
     */
    public WaypointPlayer(JSONObject jsonPlayer) {
        this.identifier = new PlayerIdentifier(UUID.fromString(jsonPlayer.get("uuid").toString()), jsonPlayer.get("username").toString());
        this.waypoints = new HashMap<>();

        JSONArray jsonWaypoints = (JSONArray) jsonPlayer.get("waypoints");
        for (JSONObject jsonWaypoint : (Iterable<JSONObject>) jsonWaypoints)
            this.waypoints.put(new WaypointCoord(jsonWaypoint), new Waypoint(jsonWaypoint));

        this.isTeleporting = false;
    }

    /**
     * @return uuid
     */
    public UUID getUUID() {
        return this.identifier.getUUID();
    }

    /**
     * @return username
     */
    public String getUsername() {
        return this.identifier.getUsername();
    }

    /**
     * @param username
     */
    public void setUsername(String username) {
        this.identifier.setUsername(username);
    }

    /**
     * Returns a private waypoint
     * @param coord
     */
    public Waypoint getWaypoint(WaypointCoord coord) {
        return this.waypoints.get(coord);
    }

    /**
     * @return waypoints
     */
    public HashMap<WaypointCoord, Waypoint> getWaypoints() {
        return waypoints;
    }

    /**
     * @param waypoint
     */
    public void addWaypoint(Waypoint waypoint) {
        this.waypoints.put(waypoint.getCoord(), waypoint);
    }

    /**
     * @param coord
     */
    public Waypoint removeWaypoint(WaypointCoord coord) {
        return this.waypoints.remove(coord);
    }

    /**
     * @return isTeleporting
     */
    public boolean isTeleporting() {
        return isTeleporting;
    }

    /**
     * @param isTeleporting
     */
    public void setTeleporting(boolean isTeleporting) {
        this.isTeleporting = isTeleporting;
    }

    /**
     * @return jsonWaypointPlayer
     */
    public JSONObject toJSON() {
        JSONObject playerData = new JSONObject();
        playerData.put("uuid", this.identifier.getUUID().toString());
        playerData.put("username", this.identifier.getUsername());
        JSONArray jsonPlayerWaypoints = new JSONArray();
        for (Waypoint waypoint : this.waypoints.values()) {
            if (waypoint != null)
                jsonPlayerWaypoints.add(waypoint.toJSON());
        }
        playerData.put("waypoints", jsonPlayerWaypoints);
        return playerData;
    }
}