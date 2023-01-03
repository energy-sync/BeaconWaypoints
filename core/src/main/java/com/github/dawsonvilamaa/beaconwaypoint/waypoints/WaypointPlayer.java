package com.github.dawsonvilamaa.beaconwaypoint.waypoints;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.UUID;

public class WaypointPlayer {
    private UUID uuid;
    private HashMap<WaypointCoord, Waypoint> waypoints;
    private boolean isTeleporting;

    /**
     * @param uuid
     */
    public WaypointPlayer(UUID uuid) {
        this.uuid = uuid;
        this.waypoints = new HashMap<>();
        this.isTeleporting = false;
    }

    /**
     * @param jsonPlayer
     */
    public WaypointPlayer(JSONObject jsonPlayer) {
        this.uuid = UUID.fromString(jsonPlayer.get("uuid").toString());
        this.waypoints = new HashMap<>();

        this.uuid = UUID.fromString(jsonPlayer.get("uuid").toString());
        JSONArray jsonWaypoints = (JSONArray) jsonPlayer.get("waypoints");
        for (Object jsonWaypoint : jsonWaypoints)
            this.waypoints.put(new WaypointCoord((JSONObject) jsonWaypoint), new Waypoint((JSONObject) jsonWaypoint));

        this.isTeleporting = false;
    }

    /**
     * @return uuid
     */
    public UUID getUUID() {
        return uuid;
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
        playerData.put("uuid", this.uuid.toString());
        JSONArray jsonPlayerWaypoints = new JSONArray();
        for (Waypoint waypoint : this.waypoints.values())
            if (waypoint != null)
                jsonPlayerWaypoints.add(waypoint.toJSON());
        playerData.put("waypoints", jsonPlayerWaypoints);
        return playerData;
    }
}