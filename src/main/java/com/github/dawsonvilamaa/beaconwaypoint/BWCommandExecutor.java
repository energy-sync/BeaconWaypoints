package com.github.dawsonvilamaa.beaconwaypoint;

import com.github.dawsonvilamaa.beaconwaypoint.gui.GUIs;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.Waypoint;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointCoord;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BWCommandExecutor implements CommandExecutor {
    private final Main plugin;

    public BWCommandExecutor(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String cleanCmd = cmd.getName().toLowerCase();

            if (cleanCmd.equals("waypoint") || cleanCmd.equals("wp")) {
                if (args.length < 2)
                    return false;
                if (!(args[1].equalsIgnoreCase("public") || args[1].equalsIgnoreCase("private")))
                    return false;

                Location playerLoc = player.getLocation();
                playerLoc.setY(playerLoc.getY() - 1);

                //check if waypoint already exists at location
                boolean waypointExists = false;
                if (args[1].equalsIgnoreCase("public")) {
                    if (Main.waypointManager.getPublicWaypoint(playerLoc) != null)
                        waypointExists = true;
                } else {
                    if (Main.waypointManager.getPrivateWaypoint(player.getUniqueId(), playerLoc) != null)
                        waypointExists = true;
                }

                if (waypointExists) {
                    player.sendMessage(ChatColor.RED + "A waypoint already exists at that location.");
                    return true;
                }

                //check if waypoint with that name already exists
                if (args[1].equalsIgnoreCase("public")) {
                    for (Waypoint waypoint : Main.waypointManager.getPublicWaypoints().values()) {
                        if (waypoint != null && waypoint.getName().equals(args[0])) {
                            player.sendMessage(ChatColor.RED + "There is already a waypoint of that name");
                            return true;
                        }
                    }
                } else {
                    for (Waypoint waypoint : Main.waypointManager.getPrivateWaypoints(player.getUniqueId()).values()) {
                        if (waypoint != null && waypoint.getName().equals(args[0])) {
                            player.sendMessage(ChatColor.RED + "There is already a waypoint of that name");
                            return true;
                        }
                    }
                }

                //check if player is standing on a beacon
                if (player.getWorld().getBlockAt(playerLoc).getType() == Material.BEACON) {

                    //add waypoint
                    WaypointCoord newWaypointCoord = new WaypointCoord(playerLoc);
                    Waypoint newWaypoint = new Waypoint(args[0], playerLoc);

                    if (args[1].equalsIgnoreCase("public"))
                        Main.waypointManager.addPublicWaypoint(newWaypoint);
                    else {
                        WaypointPlayer waypointPlayer = Main.waypointManager.getPlayer(player.getUniqueId());
                        if (waypointPlayer != null)
                            waypointPlayer.addWaypoint(newWaypoint);
                    }
                    player.sendMessage(ChatColor.GREEN + "Created the " + (args[1].equalsIgnoreCase("public") ? "public" : "private") + " waypoint " + ChatColor.BOLD + args[0]);
                    GUIs.waypointIconPickerMenu(player, newWaypoint);
                    return true;
                }
                else player.sendMessage(ChatColor.RED + "You must be standing on a beacon to set a waypoint");
                return true;
            }
        }
        return false;
    }
}