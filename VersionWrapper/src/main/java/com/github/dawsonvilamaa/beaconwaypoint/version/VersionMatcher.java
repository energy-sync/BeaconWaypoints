package com.github.dawsonvilamaa.beaconwaypoint.version;

import org.bukkit.Bukkit;

//based on AnvilGUI VersionMatcher: https://github.com/WesJD/AnvilGUI/blob/master/api/src/main/java/net/wesjd/anvilgui/version/VersionMatcher.java
public class VersionMatcher {
    public VersionWrapper match() {
        final String serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
        Bukkit.getLogger().info("[BeaconWaypoints] Loading module for version " + serverVersion);
        try {
            return (VersionWrapper) Class.forName(getClass().getPackage().getName() + ".Version_" + serverVersion).newInstance();
        } catch (IllegalAccessException | InstantiationException exception) {
            throw new IllegalStateException("Failed to instantiate version wrapper for version " + serverVersion, exception);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("BeaconWaypoints does not support server version \"" + serverVersion + "\"", exception);
        }
    }
}
