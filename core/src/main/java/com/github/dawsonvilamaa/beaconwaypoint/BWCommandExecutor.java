package com.github.dawsonvilamaa.beaconwaypoint;

import com.github.dawsonvilamaa.beaconwaypoint.gui.GUIs;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.Waypoint;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointCoord;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointPlayer;
import org.bukkit.Bukkit;
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

            if (cleanCmd.equals("waypoint") || cleanCmd.equals("wp") || cleanCmd.equals("waypoints")) {
                if (args.length == 0)
                    return false;

                //reload
                if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("BeaconWaypoints.reload")) {
                        Main.plugin.reloadConfig();
                        Main.plugin.loadLanguage();
                        player.sendMessage(ChatColor.GREEN + Main.plugin.getLanguageManager().getString("config-reloaded"));
                    }
                    else
                        player.sendMessage(ChatColor.RED + Main.plugin.getLanguageManager().getString("no-command-permission"));
                    return true;
                }

                //check if player has permission to create public waypoints
                if (!player.hasPermission("BeaconWaypoints.createWaypoints")) {
                    player.sendMessage(ChatColor.RED + Main.plugin.getLanguageManager().getString("no-command-permission"));
                    return true;
                }

                //check if player has permission to create private waypoints
                boolean privateWaypoint = false;
                if (args[args.length - 1].equalsIgnoreCase("private")) {
                    privateWaypoint = true;
                    if (!player.hasPermission("BeaconWaypoints.createWaypoints") || !player.hasPermission("BeaconWaypoints.usePrivateWaypoints")) {
                        player.sendMessage(ChatColor.RED + Main.plugin.getLanguageManager().getString("no-private-waypoint-permission"));
                        return true;
                    }
                }

                Location playerLoc = player.getLocation();
                playerLoc.setY(playerLoc.getY() - 1);

                //check if player is standing on a beacon
                if (player.getWorld().getBlockAt(playerLoc).getType() != Material.BEACON) {
                    player.sendMessage(ChatColor.RED + Main.plugin.getLanguageManager().getString("stand-on-beacon"));
                    return true;
                }

                //check if the beacon is in an allowed world
                if (!plugin.getConfig().contains("allow-all-worlds"))
                    plugin.getConfig().set("allow-all-worlds", true);
                if (!plugin.getConfig().contains("allowed-worlds"))
                    plugin.getConfig().set("allowed-worlds", Waypoint.DEFAULT_ALLOWED_WORLDS);
                if (!plugin.getConfig().getBoolean("allow-all-worlds") && !plugin.getConfig().getStringList("allowed-worlds").contains(player.getWorld().getName())) {
                    player.sendMessage(ChatColor.RED + Main.plugin.getLanguageManager().getString("world-not-allowed"));
                    return true;
                }

                //check if waypoint already exists at location
                boolean waypointExists = false;
                if (!privateWaypoint) {
                    //check if player is owner
                    Waypoint inactiveWaypoint = Main.waypointManager.getInactiveWaypoints().get(new WaypointCoord(playerLoc));
                    if (inactiveWaypoint != null && !inactiveWaypoint.getOwnerUUID().equals(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + Main.plugin.getLanguageManager().getString("not-owner"));
                        return true;
                    }

                    //check if public waypoint list is full
                    if (!plugin.getConfig().contains("max-public-waypoints"))
                        plugin.getConfig().set("max-public-waypoints", 100);
                    else {
                        int maxPublicWaypoints = plugin.getConfig().getInt("max-public-waypoints");
                        if (maxPublicWaypoints < 0)
                            maxPublicWaypoints = 0;
                        if (Main.waypointManager.getPublicWaypoints().values().size() == maxPublicWaypoints) {
                            player.sendMessage(ChatColor.RED + Main.plugin.getLanguageManager().getString("public-list-full"));
                            return true;
                        }
                    }

                    if (Main.waypointManager.getPublicWaypoint(playerLoc) != null)
                        waypointExists = true;
                } else {
                    //check if private waypoint list is full
                    if (!plugin.getConfig().contains("max-private-waypoints"))
                        plugin.getConfig().set("max-private-waypoints", 30);
                    else {
                        int maxPrivateWaypoints = plugin.getConfig().getInt("max-private-waypoints");
                        if (maxPrivateWaypoints < 0)
                            maxPrivateWaypoints = 0;
                        if (Main.waypointManager.getPrivateWaypoints(player.getUniqueId()).values().size() == maxPrivateWaypoints) {
                            player.sendMessage(ChatColor.RED + Main.plugin.getLanguageManager().getString("private-list-full"));
                            return true;
                        }
                        if (Main.waypointManager.getPrivateWaypoint(player.getUniqueId(), playerLoc) != null)
                            waypointExists = true;
                    }
                }

                if (waypointExists) {
                    player.sendMessage(ChatColor.RED + Main.plugin.getLanguageManager().getString("already-exists-at-location"));
                    return true;
                }

                //check if name is alphanumeric (including spaces, underscores, and hyphens) and is 30 characters or fewer
                StringBuilder fullWaypointName = new StringBuilder();
                for (int argIndex = 0; argIndex < args.length; argIndex++)
                    fullWaypointName.append(args[argIndex]).append(" ");
                fullWaypointName.setLength(fullWaypointName.length() - 1);
                if (fullWaypointName.toString().endsWith(" public"))
                    fullWaypointName.replace(fullWaypointName.length() - 7, fullWaypointName.length(), "");
                if (fullWaypointName.toString().endsWith(" private"))
                    fullWaypointName.replace(fullWaypointName.length() - 8, fullWaypointName.length(), "");
                if (fullWaypointName.length() > 30 || !fullWaypointName.toString().matches("^[A-Za-z0-9- ]+$")) {
                    player.sendMessage(ChatColor.RED + Main.plugin.getLanguageManager().getString("invalid-name"));
                    return true;
                }

                //check if waypoint with that name already exists
                if (!privateWaypoint) {
                    for (Waypoint waypoint : Main.waypointManager.getPublicWaypoints().values()) {
                        if (waypoint != null && waypoint.getName().equals(fullWaypointName.toString())) {
                            player.sendMessage(ChatColor.RED + Main.plugin.getLanguageManager().getString("name-taken"));
                            return true;
                        }
                    }
                } else {
                    for (Waypoint waypoint : Main.waypointManager.getPrivateWaypoints(player.getUniqueId()).values()) {
                        if (waypoint != null && waypoint.getName().equals(fullWaypointName.toString())) {
                            player.sendMessage(ChatColor.RED + Main.plugin.getLanguageManager().getString("private-name-taken"));
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

                if (!privateWaypoint)
                    Main.waypointManager.addPublicWaypoint(newWaypoint);
                else {
                    WaypointPlayer waypointPlayer = Main.waypointManager.getPlayer(player.getUniqueId());
                    if (waypointPlayer != null)
                        waypointPlayer.addWaypoint(newWaypoint);
                }
                player.sendMessage(ChatColor.GREEN + (privateWaypoint ? Main.plugin.getLanguageManager().getString("created-private-waypoint") : Main.plugin.getLanguageManager().getString("created-public-waypoint")) + " " + ChatColor.BOLD + fullWaypointName);
                GUIs.waypointIconPickerMenu(player, newWaypoint, null);
                return true;
            }
        }
        return false;
    }
}