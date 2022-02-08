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
                if (!(args[args.length - 1].equalsIgnoreCase("public") || args[args.length - 1].equalsIgnoreCase("private")))
                    return false;

                Location playerLoc = player.getLocation();
                playerLoc.setY(playerLoc.getY() - 1);

                //check if player is standing on a beacon
                if (player.getWorld().getBlockAt(playerLoc).getType() != Material.BEACON) {
                    player.sendMessage(ChatColor.RED + "You must be standing on a beacon to set a waypoint");
                    return true;
                }

                //check if the beacon is in an allowed world
                if (!plugin.getConfig().getStringList("allowed-worlds").contains(player.getWorld().getName())) {
                    player.sendMessage(ChatColor.RED + "You cannot set a waypoint in this world");
                    return true;
                }

                //check if waypoint already exists at location
                boolean waypointExists = false;
                if (args[args.length - 1].equalsIgnoreCase("public")) {
                    //check if player is owner
                    Waypoint inactiveWaypoint = Main.waypointManager.getInactiveWaypoints().get(new WaypointCoord(playerLoc));
                    if (inactiveWaypoint != null && !inactiveWaypoint.getOwnerUUID().equals(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "Only the owner of this beacon can create a public waypoint");
                        return true;
                    }

                    //check if public waypoint list is full
                    int maxPublicWaypoints = plugin.getConfig().getInt("max-public-waypoints");
                    if (maxPublicWaypoints < 0)
                        maxPublicWaypoints = 0;
                    if (Main.waypointManager.getPublicWaypoints().values().size() == maxPublicWaypoints) {
                        player.sendMessage(ChatColor.RED + "Public waypoint list is full!");
                        return true;
                    }

                    if (Main.waypointManager.getPublicWaypoint(playerLoc) != null)
                        waypointExists = true;
                } else {
                    //check if private waypoint list is full
                    int maxPrivateWaypoints = plugin.getConfig().getInt("max-private-waypoints");
                    if (maxPrivateWaypoints < 0)
                        maxPrivateWaypoints = 0;
                    if (Main.waypointManager.getPrivateWaypoints(player.getUniqueId()).values().size() == maxPrivateWaypoints) {
                        player.sendMessage(ChatColor.RED + "Private waypoint list is full!");
                        return true;
                    }
                    if (Main.waypointManager.getPrivateWaypoint(player.getUniqueId(), playerLoc) != null)
                        waypointExists = true;
                }

                if (waypointExists) {
                    player.sendMessage(ChatColor.RED + "A waypoint already exists at that location.");
                    return true;
                }

                //check if name is alphanumeric (including spaces, underscores, and hyphens) and is 30 characters or fewer
                StringBuilder fullWaypointName = new StringBuilder();
                for (int argIndex = 0; argIndex < args.length - 1; argIndex++)
                    fullWaypointName.append(args[argIndex]).append(" ");
                fullWaypointName.setLength(fullWaypointName.length() - 1);
                if (fullWaypointName.length() > 30 || !fullWaypointName.toString().matches("^[A-Za-z0-9- ]+$")) {
                    player.sendMessage(ChatColor.RED + "Waypoint names must be 30 characters or fewer and can only contain letters, numbers, spaces, underscores, and hyphens.");
                    return true;
                }

                //check if waypoint with that name already exists
                if (args[args.length - 1].equalsIgnoreCase("public")) {
                    for (Waypoint waypoint : Main.waypointManager.getPublicWaypoints().values()) {
                        if (waypoint != null && waypoint.getName().equals(fullWaypointName.toString())) {
                            player.sendMessage(ChatColor.RED + "There is already a public waypoint of that name");
                            return true;
                        }
                    }
                } else {
                    for (Waypoint waypoint : Main.waypointManager.getPrivateWaypoints(player.getUniqueId()).values()) {
                        if (waypoint != null && waypoint.getName().equals(fullWaypointName.toString())) {
                            player.sendMessage(ChatColor.RED + "There is already a public waypoint of that name");
                            return true;
                        }
                    }
                }

                //activate waypoint
                Waypoint inactiveWaypoint = Main.waypointManager.getInactiveWaypoints().get(new WaypointCoord(playerLoc));
                Waypoint newWaypoint;
                if (inactiveWaypoint != null) {
                    newWaypoint = inactiveWaypoint.clone();
                    Main.waypointManager.getInactiveWaypoints().remove(newWaypoint.getCoord());
                }
                else {
                    newWaypoint = new Waypoint(player.getUniqueId(), new WaypointCoord(playerLoc));
                }
                newWaypoint.setName(fullWaypointName.toString());
                newWaypoint.setIsWaypoint(true);

                if (args[args.length - 1].equalsIgnoreCase("public"))
                    Main.waypointManager.addPublicWaypoint(newWaypoint);
                else {
                    WaypointPlayer waypointPlayer = Main.waypointManager.getPlayer(player.getUniqueId());
                    if (waypointPlayer != null)
                        waypointPlayer.addWaypoint(newWaypoint);
                }
                player.sendMessage(ChatColor.GREEN + "Created the " + (args[args.length - 1].equalsIgnoreCase("public") ? "public" : "private") + " waypoint " + ChatColor.BOLD + fullWaypointName);
                GUIs.waypointIconPickerMenu(player, newWaypoint, null);
                return true;
            }
        }
        return false;
    }
}