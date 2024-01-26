package com.github.dawsonvilamaa.beaconwaypoint.waypoints;

import java.util.UUID;

public class PlayerIdentifier {
    private UUID uuid;
    private String username;

    public PlayerIdentifier(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    /**
     * @return uuid
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * @param uuid
     */
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * @return username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }
}