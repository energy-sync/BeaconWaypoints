package com.github.dawsonvilamaa.beaconwaypoint;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

            switch (cleanCmd) {
                case "waypoint":
                case "wp":
                    if (args.length < 1) return false;
                    if (args[0].toLowerCase().equals("add") || args[0].toLowerCase().equals("create")) {
                        if (args.length < 2) return false;
                        for (Waypoint waypoint : Main.waypoints) {
                            if (waypoint != null) {
                                if (waypoint.getName().equals(args[1])) {
                                    player.sendMessage(ChatColor.RED + "There is already a waypoint of that name");
                                    return true;
                                }
                            }
                        }
                        Location playerLoc = player.getLocation();
                        playerLoc.setY(playerLoc.getY() - 1);
                        if (player.getWorld().getBlockAt(playerLoc).getType() == Material.BEACON) {
                            Waypoint newWaypoint = new Waypoint(args[1], playerLoc);
                            Main.waypoints.add(newWaypoint);
                            player.sendMessage(ChatColor.GREEN + "Created the waypoint " + ChatColor.BOLD + args[1]);
                            return true;
                        }
                        else player.sendMessage(ChatColor.RED + "You must be standing on a beacon to set a waypoint");
                        return true;
                    }
                    else if (args[0].toLowerCase().equals("tp")) {
                        if (args.length < 2) return false;
                        Location playerLoc = player.getLocation();
                        playerLoc.setY(playerLoc.getY() - 1);
                        if (player.getWorld().getBlockAt(playerLoc).getType() == Material.BEACON) {
                            for (Waypoint waypoint : Main.waypoints) {
                                if (waypoint != null) {
                                    if (waypoint.getName().equals(args[1])) {
                                        if (player.getWorld().getBlockAt(waypoint.getX(), waypoint.getY(), waypoint.getZ()).getType() != Material.BEACON)
                                            player.sendMessage(ChatColor.RED + "Cannot find a beacon for that waypoint. Either it was destroyed or it is in another dimension.");
                                        else {
                                            player.sendMessage(ChatColor.YELLOW + "Teleported to " + ChatColor.BOLD + waypoint.getName());
                                            Location tpLoc = new Location(player.getWorld(), (double) (waypoint.getX() + 0.5), (double) (waypoint.getY() + 1), (double) (waypoint.getZ() + 0.5), player.getLocation().getYaw(), player.getLocation().getPitch());
                                            player.teleport(tpLoc);
                                        }
                                        return true;
                                    }
                                }
                            }
                            player.sendMessage(ChatColor.RED + "No waypoint with that name found");
                            return true;
                        }
                        else player.sendMessage(ChatColor.RED + "You must be standing on a beacon to teleport to a waypoint");
                        return true;
                    }
                    else if (args[0].toLowerCase().equals("remove") || args[0].toLowerCase().equals("delete")) {
                        if (args.length < 2) return false;
                        for (int i = 0; i < Main.waypoints.size(); i++) {
                            if (Main.waypoints.get(i) != null) {
                                if (Main.waypoints.get(i).getName().equals(args[1].toLowerCase())) {
                                    Main.waypoints.set(i, null);
                                    player.sendMessage(ChatColor.GREEN + "Removed waypoint " + ChatColor.BOLD + args[1]);
                                    return true;
                                }
                            }
                        }
                        player.sendMessage(ChatColor.RED + "No waypoint with that name was found");
                        return true;
                    }
                    else if (args[0].toLowerCase().equals("list")) {
                        String listStr = ChatColor.YELLOW + "List of waypoints:" + ChatColor.WHITE;
                        for (Waypoint waypoint : Main.waypoints) {
                            if (waypoint != null) listStr += "\n" + waypoint.getName();
                        }
                        player.sendMessage(listStr);
                        return true;
                    }

                default:
                    return false;
            }
        }
        else return true;
    }
}