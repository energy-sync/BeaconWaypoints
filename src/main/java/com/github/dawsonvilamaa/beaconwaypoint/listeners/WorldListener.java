package com.github.dawsonvilamaa.beaconwaypoint.listeners;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.gui.GUIs;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.Waypoint;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointCoord;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Objects;

public class WorldListener implements Listener {

    /**
     */
    public WorldListener() {

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
            WaypointCoord waypointCoord = new WaypointCoord(e.getBlock().getLocation());

            //remove public waypoint
            Waypoint publicWaypoint = Main.waypointManager.getPublicWaypoint(waypointCoord);
            if (publicWaypoint != null) {
                Main.waypointManager.removePublicWaypoint(waypointCoord);
                e.getPlayer().sendMessage(ChatColor.RED + "Removed public waypoint " + ChatColor.BOLD + publicWaypoint.getName());
            }

            //remove private waypoints
            for (WaypointPlayer waypointPlayer : Main.waypointManager.getWaypointPlayers().values()) {
                Waypoint privateWaypoint = Main.waypointManager.getPrivateWaypoint(waypointPlayer.getUUID(), waypointCoord);
                if (privateWaypoint != null) {
                    Main.waypointManager.removePrivateWaypoint(waypointPlayer.getUUID(), waypointCoord);
                    e.getPlayer().sendMessage(ChatColor.RED + "Removed private waypoint " + ChatColor.BOLD + privateWaypoint.getName());
                }
            }
        }
    }

    //when player opens a beacon
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND && Objects.requireNonNull(e.getClickedBlock()).getType() == Material.BEACON && e.getPlayer().isSneaking()) {
            WaypointCoord waypointCoord = new WaypointCoord(e.getClickedBlock().getLocation());
            Waypoint waypoint = Main.waypointManager.getPublicWaypoint(waypointCoord);
            if (waypoint == null)
                waypoint = Main.waypointManager.getPrivateWaypoint(e.getPlayer().getUniqueId(), waypointCoord);
            if (waypoint != null) {
                int beaconStatus = waypoint.getBeaconStatus();
                if (beaconStatus != 0)
                    GUIs.privateOrPublicMenu(e.getPlayer(), waypoint);
                else e.getPlayer().sendMessage(ChatColor.RED + "Unable to teleport with this beacon. It either does not have a pyramid underneath it, or something is obstructing the beam.");
                e.setCancelled(true);
            }
        }
    }

    //stop block from being placed when waypoint beacon is interacted with (sneak click)
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.BEACON && e.getPlayer().isSneaking()) {
            WaypointCoord waypointCoord = new WaypointCoord(e.getBlock().getLocation());
            Waypoint waypoint = Main.waypointManager.getPublicWaypoint(waypointCoord);
            if (waypoint == null)
                waypoint = Main.waypointManager.getPrivateWaypoint(e.getPlayer().getUniqueId(), waypointCoord);
            if (waypoint != null)
                e.setCancelled(true);
        }
    }
}