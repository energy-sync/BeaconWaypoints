package com.github.dawsonvilamaa.beaconwaypoint.version;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public interface VersionWrapper {
    /**
     * Opens the vanilla beacon GUI for a player
     *
     * @param beacon
     * @param player
     */
    void openBeaconMenu(Block beacon, Player player);

    /**
     * Returns the blocks available to be used in a beacon pyramid
     *
     * @return
     */
    List<Material> getPyramidBlocks();

    /**
     * Sends a player a message notification for a plugin update
     *
     * @param player
     */
    void sendOpUpdateMessage(Player player);
}
