package com.github.dawsonvilamaa.beaconwaypoint.waypoints;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.UUID;

public class WaypointPlayer {
    private UUID uuid;
    private HashMap<WaypointCoord, Waypoint> waypoints;

    /**
     * @param uuid
     */
    public WaypointPlayer(UUID uuid) {
        this.uuid = uuid;
        this.waypoints = new HashMap<WaypointCoord, Waypoint>();
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
}
