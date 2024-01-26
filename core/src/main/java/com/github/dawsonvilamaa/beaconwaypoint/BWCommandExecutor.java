package com.github.dawsonvilamaa.beaconwaypoint;

import com.github.dawsonvilamaa.beaconwaypoint.gui.GUIs;
import com.github.dawsonvilamaa.beaconwaypoint.gui.InventoryGUIButton;
import com.github.dawsonvilamaa.beaconwaypoint.gui.MultiPageInventoryGUI;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

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

                Main plugin = Main.getPlugin();
                WaypointManager waypointManager = Main.getWaypointManager();
                LanguageManager languageManager = Main.getLanguageManager();

                //reload
                if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("BeaconWaypoints.reload")) {
                        plugin.reloadConfig();
                        plugin.loadLanguage();
                        player.sendMessage(ChatColor.GREEN + languageManager.getString("config-reloaded"));
                    }
                    else
                        player.sendMessage(ChatColor.RED + languageManager.getString("no-command-permission"));
                    return true;
                }

                //share private waypoint
                if (args[0].equalsIgnoreCase("share")) {
                    //check if player has permission to use private waypoints
                    if (!player.hasPermission("BeaconWaypoints.useWaypoints")) {
                        player.sendMessage(ChatColor.RED + languageManager.getString("no-private-waypoint-permission"));
                        return true;
                    }

                    //check if player has any private waypoints
                    if (waypointManager.getPlayer(player.getUniqueId()).getWaypoints().isEmpty()) {
                        player.sendMessage(ChatColor.RED + languageManager.getString("no-private-waypoints"));
                        return true;
                    }

                    if (args.length < 2)
                        return false;

                    //CHECK IF THE PLAYER ALREADY HAS THIS WAYPOINT SHARED

                    UUID playerUUID;

                    //username
                    OfflinePlayer sharedBukkitPlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (sharedBukkitPlayer == null) {
                        player.sendMessage(ChatColor.RED + languageManager.getString("player-not-found"));
                        return true;
                    }
                    playerUUID = sharedBukkitPlayer.getUniqueId();

                    if (playerUUID.equals(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + languageManager.getString("self-share"));
                        return true;
                    }

                    //player chooses waypoint to share
                    GUIs.sharePrivateWaypointMenu(player, playerUUID, args[1]);
                    return true;
                }

                //check if player has permission to create public waypoints
                if (!player.hasPermission("BeaconWaypoints.createWaypoints")) {
                    player.sendMessage(ChatColor.RED + languageManager.getString("no-command-permission"));
                    return true;
                }

                //check if player has permission to create private waypoints
                boolean privateWaypoint = false;
                if (args[args.length - 1].equalsIgnoreCase("private")) {
                    privateWaypoint = true;
                    if (!player.hasPermission("BeaconWaypoints.createWaypoints") || !player.hasPermission("BeaconWaypoints.usePrivateWaypoints")) {
                        player.sendMessage(ChatColor.RED + languageManager.getString("no-private-waypoint-permission"));
                        return true;
                    }
                }

                Location playerLoc = player.getLocation();
                playerLoc.setY(playerLoc.getY() - 1);

                //check if player is standing on a beacon
                if (player.getWorld().getBlockAt(playerLoc).getType() != Material.BEACON) {
                    player.sendMessage(ChatColor.RED + languageManager.getString("stand-on-beacon"));
                    return true;
                }

                //check if the beacon is in an allowed world
                if (!plugin.getConfig().contains("allow-all-worlds"))
                    plugin.getConfig().set("allow-all-worlds", true);
                if (!plugin.getConfig().contains("allowed-worlds"))
                    plugin.getConfig().set("allowed-worlds", WaypointHelper.DEFAULT_ALLOWED_WORLDS);
                if (!plugin.getConfig().getBoolean("allow-all-worlds") && !plugin.getConfig().getStringList("allowed-worlds").contains(player.getWorld().getName())) {
                    player.sendMessage(ChatColor.RED + languageManager.getString("world-not-allowed"));
                    return true;
                }

                //check if waypoint already exists at location
                boolean waypointExists = false;
                if (!privateWaypoint) {
                    //check if player is owner
                    Waypoint inactiveWaypoint = waypointManager.getInactiveWaypoints().get(new WaypointCoord(playerLoc));
                    if (inactiveWaypoint != null && !inactiveWaypoint.getOwnerUUID().equals(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + languageManager.getString("not-owner"));
                        return true;
                    }

                    //check if public waypoint list is full
                    if (!plugin.getConfig().contains("max-public-waypoints"))
                        plugin.getConfig().set("max-public-waypoints", 100);
                    else {
                        int maxPublicWaypoints = plugin.getConfig().getInt("max-public-waypoints");
                        if (maxPublicWaypoints < 0)
                            maxPublicWaypoints = 0;
                        if (waypointManager.getPublicWaypoints().values().size() == maxPublicWaypoints) {
                            player.sendMessage(ChatColor.RED + languageManager.getString("public-list-full"));
                            return true;
                        }
                    }

                    if (waypointManager.getPublicWaypoint(playerLoc) != null)
                        waypointExists = true;
                } else {
                    //check if private waypoint list is full
                    if (!plugin.getConfig().contains("max-private-waypoints"))
                        plugin.getConfig().set("max-private-waypoints", 30);
                    else {
                        int maxPrivateWaypoints = plugin.getConfig().getInt("max-private-waypoints");
                        if (maxPrivateWaypoints < 0)
                            maxPrivateWaypoints = 0;
                        if (waypointManager.getPrivateWaypoints(player.getUniqueId()).values().size() == maxPrivateWaypoints) {
                            player.sendMessage(ChatColor.RED + languageManager.getString("private-list-full"));
                            return true;
                        }
                        if (waypointManager.getPrivateWaypoint(player.getUniqueId(), playerLoc) != null)
                            waypointExists = true;
                    }
                }

                if (waypointExists) {
                    player.sendMessage(ChatColor.RED + languageManager.getString("already-exists-at-location"));
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
                boolean forceAlphanumeric = plugin.getConfig().getBoolean("force-alphanumeric-names");
                if (fullWaypointName.length() > 30 || (forceAlphanumeric && !fullWaypointName.toString().matches("^[A-Za-z0-9- ]+$"))) {
                    player.sendMessage(ChatColor.RED + languageManager.getString(plugin.getConfig().getBoolean("force-alphanumeric-names") ? "invalid-name-alphanumeric" : "invalid-name"));
                    return true;
                }

                //check if waypoint with that name already exists
                if (!privateWaypoint) {
                    for (Waypoint waypoint : waypointManager.getPublicWaypoints().values()) {
                        if (waypoint != null && waypoint.getName().equals(fullWaypointName.toString())) {
                            player.sendMessage(ChatColor.RED + languageManager.getString("name-taken"));
                            return true;
                        }
                    }
                } else {
                    for (Waypoint waypoint : waypointManager.getPrivateWaypoints(player.getUniqueId()).values()) {
                        if (waypoint != null && waypoint.getName().equals(fullWaypointName.toString())) {
                            player.sendMessage(ChatColor.RED + languageManager.getString("private-name-taken"));
                            return true;
                        }
                    }
                }

                //activate waypoint
                Waypoint inactiveWaypoint = waypointManager.getInactiveWaypoints().get(new WaypointCoord(playerLoc));
                Waypoint newWaypoint;
                if (inactiveWaypoint != null) {
                    newWaypoint = inactiveWaypoint.clone();
                    waypointManager.getInactiveWaypoints().remove(newWaypoint.getCoord());
                }
                else {
                    newWaypoint = new Waypoint(player.getUniqueId(), new WaypointCoord(playerLoc));
                }
                newWaypoint.setName(fullWaypointName.toString());
                newWaypoint.setIsWaypoint(true);

                if (!privateWaypoint)
                    waypointManager.addPublicWaypoint(newWaypoint);
                else {
                    WaypointPlayer waypointPlayer = waypointManager.getPlayer(player.getUniqueId());
                    if (waypointPlayer != null)
                        waypointPlayer.addWaypoint(newWaypoint);
                }
                player.sendMessage(ChatColor.GREEN + (privateWaypoint ? languageManager.getString("created-private-waypoint") : languageManager.getString("created-public-waypoint")) + " " + ChatColor.BOLD + fullWaypointName);
                GUIs.waypointIconPickerMenu(player, newWaypoint, null);
                return true;
            }
        }
        return false;
    }
}