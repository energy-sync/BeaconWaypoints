package com.github.dawsonvilamaa.beaconwaypoint.waypoints;

import org.bukkit.Location;

import java.util.*;

public class WaypointManager {
    private HashMap<WaypointCoord, Waypoint> publicWaypoints;
    private HashMap<WaypointCoord, Waypoint> pinnedWaypoints;
    private HashMap<UUID, WaypointPlayer> waypointPlayers;
    private HashMap<WaypointCoord, Waypoint> inactiveWaypoints;

    public WaypointManager() {
        publicWaypoints = new HashMap<>();
        pinnedWaypoints = new HashMap<>();
        waypointPlayers = new HashMap<>();
        inactiveWaypoints = new HashMap<>();
    }

    /**
     * Adds a waypoint to the public waypoint list
     * @param waypoint
     */
    public void addPublicWaypoint(Waypoint waypoint) {
        this.publicWaypoints.put(waypoint.getCoord(), waypoint);
    }

    /**
     * Removes a waypoint from the public waypoint list
     * @param coord
     */
    public Waypoint removePublicWaypoint(WaypointCoord coord) {
        Waypoint waypoint = pinnedWaypoints.remove(coord);
        if (waypoint == null)
            return publicWaypoints.remove(coord);
        return waypoint;
    }

    /**
     * Adds a waypoint to the pinned group
     * @param waypoint
     */
    public void pinWaypoint(Waypoint waypoint) {
        WaypointCoord coord = waypoint.getCoord();
        this.pinnedWaypoints.put(coord, this.publicWaypoints.get(coord));
        this.publicWaypoints.remove(coord);
        this.pinnedWaypoints.get(coord).setPinned(true);
    }

    /**
     * Removes a waypoint from the pinned group
     * @param waypoint
     */
    public void unpinWaypoint(Waypoint waypoint) {
        WaypointCoord coord = waypoint.getCoord();
        this.publicWaypoints.put(coord, this.pinnedWaypoints.get(coord));
        this.pinnedWaypoints.remove(coord);
        this.publicWaypoints.get(coord).setPinned(false);
    }

    /**
     * Adds a private waypoint for a player
     * @param uuid
     * @param waypoint
     */
    public void addPrivateWaypoint(UUID uuid, Waypoint waypoint) {
        if (!waypointPlayers.containsKey(uuid)) {
            addPlayer(uuid);
        }
        waypointPlayers.get(uuid).addWaypoint(waypoint);
    }

    /**
     * Removes a waypoint from a player's private waypoint list
     * @param uuid
     * @param coord
     */
    public Waypoint removePrivateWaypoint(UUID uuid, WaypointCoord coord) {
        if (waypointPlayers.containsKey(uuid))
            return waypointPlayers.get(uuid).removeWaypoint(coord);
        return null;
    }

    /**
     * Returns a waypoint from the public waypoint list
     * @param coord
     */
    public Waypoint getPublicWaypoint(WaypointCoord coord) {
        Waypoint waypoint = pinnedWaypoints.get(coord);
        if (waypoint == null)
            return publicWaypoints.get(coord);
        return waypoint;
    }

    /**
     * Returns a waypoint from the public waypoint list
     * @param location
     */
    public Waypoint getPublicWaypoint(Location location) {
        return publicWaypoints.get(new WaypointCoord(location));
    }

    /**
     * Returns a HashMap of all public waypoints
     * @return waypoints
     */
    public HashMap<WaypointCoord, Waypoint> getPublicWaypoints() {
        return publicWaypoints;
    }

    /**
     * Returns a collection of all public waypoints sorted alphabetically
     * @return waypoints
     */
    public Collection<Waypoint> getPublicWaypointsSortedAlphabetically() {
        return sortWaypointsAlphabetically(this.publicWaypoints.values());
    }

    /**
     * Add a waypoint to the pinned list
     * @param waypoint
     */
    public void addPinnedWaypoint(Waypoint waypoint) {
        pinnedWaypoints.put(waypoint.getCoord(), waypoint);
    }

    /**
     * Returns a waypoint from the pinned waypoint list
     * @param coord
     * @return
     */
    public Waypoint getPinnedWaypoint(WaypointCoord coord) {
        return pinnedWaypoints.get(coord);
    }

    /**
     * Returns a HashMap of all pinned waypoints
     * @return pinnedWaypoints
     */
    public HashMap<WaypointCoord, Waypoint> getPinnedWaypoints() {
        return pinnedWaypoints;
    }

    /**
     * Returns a collection of all pinned waypoints sorted alphabetically
     * @return
     */
    public Collection<Waypoint> getPinnedWaypointsSortedAlphabetically() {
        return sortWaypointsAlphabetically(this.pinnedWaypoints.values());
    }

    /**
     * Returns a waypoint from a player's private waypoint list
     * @param uuid
     * @param coord
     */
    public Waypoint getPrivateWaypoint(UUID uuid, WaypointCoord coord) {
        if (waypointPlayers.containsKey(uuid))
            return waypointPlayers.get(uuid).getWaypoint(coord);
        return null;
    }

    /**
     * Returns a waypoint from a player's private waypoint list
     * @param uuid
     * @param location
     */
    public Waypoint getPrivateWaypoint(UUID uuid, Location location) {
        if (waypointPlayers.containsKey(uuid))
            return waypointPlayers.get(uuid).getWaypoint(new WaypointCoord(location));
        return null;
    }

    /**
     * Returns a HashMap of all private waypoints for a player
     * @return waypoints
     */
    public HashMap<WaypointCoord, Waypoint> getPrivateWaypoints(UUID uuid) {
        if (waypointPlayers.containsKey(uuid))
            return waypointPlayers.get(uuid).getWaypoints();
        else return null;
    }

    /**
     * Returns all private waypoints at a location
     * @param waypointCoord
     * @return privateWaypoints
     */
    public List<Waypoint> getPrivateWaypointsAtCoord(WaypointCoord waypointCoord) {
        List<Waypoint> privateWaypoints = new ArrayList<>();
        for (WaypointPlayer waypointPlayer : waypointPlayers.values()) {
            Waypoint privateWaypoint = waypointPlayer.getWaypoint(waypointCoord);
            if (privateWaypoint != null)
                privateWaypoints.add(privateWaypoint);
        }
        return privateWaypoints;
    }

    /**
     * Returns the total number of private waypoints between all players
     * @return numPrivateWaypoints
     */
    public int getNumPrivateWaypoints() {
        int numPrivateWaypoints = 0;
        for (WaypointPlayer waypointPlayer : waypointPlayers.values())
            numPrivateWaypoints += waypointPlayer.getWaypoints().size();
        return numPrivateWaypoints;
    }

    /**
     * Returns a collection of all private waypoints for a player sorted alphabetically
     * @param uuid
     * @return waypoints
     */
    public Collection<Waypoint> getPrivateWaypointsSortedAlphabetically(UUID uuid) {
        return sortWaypointsAlphabetically(waypointPlayers.get(uuid).getWaypoints().values());
    }

    /**
     * Returns a HashMap of all waypoints where the beacon has been placed, but a waypoint has not been created
     * @return
     */
    public HashMap<WaypointCoord, Waypoint> getInactiveWaypoints() {
        return this.inactiveWaypoints;
    }

    /**
     * @param waypoint
     */
    public void addInactiveWaypoint(Waypoint waypoint) {
        this.inactiveWaypoints.put(waypoint.getCoord(), waypoint);
    }

    /**
     * @param coord waypoint
     */
    public void removeInactiveWaypoint(WaypointCoord coord) {
        this.inactiveWaypoints.remove(coord);
    }

    /**
     * @param coord
     * @return waypoint
     */
    public Waypoint getInactiveWaypoint(WaypointCoord coord) {
        return this.inactiveWaypoints.get(coord);
    }

    /**
     * Sorts a collection of waypoints alphabetically
     * @param waypoints
     * @return
     */
    public Collection<Waypoint> sortWaypointsAlphabetically(Collection<Waypoint> waypoints) {
        List<Waypoint> sortedWaypoints = new ArrayList<>(waypoints);
        sortedWaypoints.sort(Comparator.comparing(Waypoint::getName));
        return sortedWaypoints;
    }

    /**
     * Returns a collection of all public and private waypoints at a location
     * @param coord
     * @return waypoints
     */
    public Collection<Waypoint> getAllWaypointsAtCoord(WaypointCoord coord) {
        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(getPublicWaypoint(coord));
        for (WaypointPlayer waypointPlayer : this.waypointPlayers.values())
            waypoints.add(waypointPlayer.getWaypoint(coord));
        return waypoints;
    }

    /**
     * Adds a player to the waypoint player list
     * @param uuid
     */
    public void addPlayer(UUID uuid) {
        waypointPlayers.put(uuid, new WaypointPlayer(uuid));
    }

    /**
     * Returns a WaypointPlayer from the waypoint player list
     * @param uuid
     * @return waypointPlayer
     */
    public WaypointPlayer getPlayer(UUID uuid) {
        return waypointPlayers.get(uuid);
    }

    /**
     * @return the waypointPlayers
     */
    public HashMap<UUID, WaypointPlayer> getWaypointPlayers() {
        return waypointPlayers;
    }
}