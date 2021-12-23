package com.github.dawsonvilamaa.beaconwaypoint.listeners;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.gui.GUIs;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.Waypoint;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointCoord;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointPlayer;
import com.github.dawsonvilamaa.beaconwaypoint.gui.InventoryGUI;
import com.github.dawsonvilamaa.beaconwaypoint.gui.InventoryGUIButton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class WorldListener implements Listener {
    private Main plugin;

    /**
     * @param plugin
     */
    public WorldListener(Main plugin) {
        this.plugin = plugin;
    }

    //adds players to waypointPlayers map when they join if they are already not in the map
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        WaypointPlayer waypointPlayer = Main.waypointManager.getPlayer(e.getPlayer().getUniqueId());

        //add if not in map
        if (waypointPlayer == null)
            Main.waypointManager.addPlayer(e.getPlayer().getUniqueId());
    }

    //delete waypoint when beacon is broken
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.BEACON) {
            Waypoint removedWaypoint = null;
            WaypointCoord waypointCoord = new WaypointCoord(e.getBlock().getLocation());

            //check public waypoints
            removedWaypoint = Main.waypointManager.removePublicWaypoint(waypointCoord);

            //check private waypoints
            if (removedWaypoint == null) {
                WaypointPlayer waypointPlayer = Main.waypointManager.getPlayer(e.getPlayer().getUniqueId());
                if (waypointPlayer != null)
                    removedWaypoint = waypointPlayer.getWaypoints().remove(waypointCoord);
            }

            //send message to player
            if (removedWaypoint != null)
                e.getPlayer().sendMessage(ChatColor.RED + "Removed waypoint " + ChatColor.BOLD + removedWaypoint.getName());
        }
    }

    //when player opens a beacon
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.BEACON && e.getPlayer().isSneaking()) {
            WaypointCoord waypointCoord = new WaypointCoord(e.getClickedBlock().getLocation());
            Waypoint waypoint = Main.plugin.getWaypoint(waypointCoord);
            if (waypoint != null) {
                e.setCancelled(true);
                GUIs.privateOrPublicMenu(e.getPlayer(), waypoint, e);
            }
        }
    }

    //stop block from being placed when waypoint beacon is interacted with (sneak click)
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.BEACON && e.getPlayer().isSneaking()) {
            WaypointCoord waypointCoord = new WaypointCoord(e.getBlock().getLocation());
            Waypoint waypoint = Main.plugin.getWaypoint(waypointCoord);
            if (waypoint != null)
                e.setCancelled(true);
        }
    }
}