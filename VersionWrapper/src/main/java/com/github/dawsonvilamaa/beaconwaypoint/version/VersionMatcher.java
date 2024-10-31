package com.github.dawsonvilamaa.beaconwaypoint.version;

import org.bukkit.Bukkit;

//based on AnvilGUI VersionMatcher: https://github.com/WesJD/AnvilGUI/blob/master/api/src/main/java/net/wesjd/anvilgui/version/VersionMatcher.java
public class VersionMatcher {
    public VersionWrapper match() {
        String serverVersion = Bukkit.getServer().getBukkitVersion();
        String nmsVersion = "";
        if (serverVersion.startsWith("1.14"))
            nmsVersion = "1_14_R1";
        else if (serverVersion.startsWith("1.15"))
            nmsVersion = "1_14_R1";
        else if (serverVersion.startsWith("1.16.1"))
            nmsVersion = "1_16_R1";
        else if (serverVersion.startsWith("1.16.2") || serverVersion.startsWith("1.16.3"))
            nmsVersion = "1_16_R2";
        else if (serverVersion.startsWith("1.16.4") || serverVersion.startsWith("1.16.5"))
            nmsVersion = "1_16_R3";
        else if (serverVersion.startsWith("1.17"))
            nmsVersion = "1_17_R1";
        else if (serverVersion.startsWith("1.18-") || serverVersion.startsWith("1.18.1"))
            nmsVersion = "1_18_R1";
        else if (serverVersion.startsWith("1.18.2"))
            nmsVersion = "1_18_R2";
        else if (serverVersion.startsWith("1.19-") || serverVersion.startsWith("1.19.1") || serverVersion.startsWith("1.19.2"))
            nmsVersion = "1_19_R1";
        else if (serverVersion.startsWith("1.19.3"))
            nmsVersion = "1_19_R2";
        else if (serverVersion.startsWith("1.19.4"))
            nmsVersion = "1_19_R3";
        else if (serverVersion.startsWith("1.20-") || serverVersion.startsWith("1.20.1"))
            nmsVersion = "1_20_R1";
        else if (serverVersion.startsWith("1.20.2"))
            nmsVersion = "1_20_R2";
        else if (serverVersion.startsWith("1.20.3") || serverVersion.startsWith("1.20.4"))
            nmsVersion = "1_20_R3";
        else if (serverVersion.startsWith("1.20.6"))
            nmsVersion = "1_20_R4";
        else if (serverVersion.startsWith("1.21.0") || serverVersion.startsWith("1.21.1"))
            nmsVersion = "1_21_R1";
        else if (serverVersion.startsWith("1.21.2") || serverVersion.startsWith("1.21.3"))
            nmsVersion = "1_21_R2";
        else {
            Bukkit.getLogger().severe("BeaconWaypoints does not support version " + serverVersion);
            return null;
        }

        //final String serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
        Bukkit.getLogger().info("[BeaconWaypoints] Loading module for version " + serverVersion);
        try {
            return (VersionWrapper) Class.forName(getClass().getPackage().getName() + ".Version_" + nmsVersion).newInstance();
        } catch (IllegalAccessException | InstantiationException exception) {
            throw new IllegalStateException("[BeaconWaypoints] Failed to instantiate version wrapper for version " + serverVersion + " (" + nmsVersion + ")", exception);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("BeaconWaypoints does not support server version " + serverVersion + " (" + nmsVersion + ")", exception);
        }
    }
}
