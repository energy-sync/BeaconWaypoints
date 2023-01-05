package com.github.dawsonvilamaa.beaconwaypoint.listeners;

import com.github.dawsonvilamaa.beaconwaypoint.LanguageManager;
import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.UpdateChecker;
import com.github.dawsonvilamaa.beaconwaypoint.gui.GUIs;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.Waypoint;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointCoord;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointManager;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointPlayer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class WorldListener implements Listener {

    //adds players to waypointPlayers map when they join if they are already not in the map
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Main plugin = Main.getPlugin();
        WaypointManager waypointManager = Main.getWaypointManager();
        LanguageManager languageManager = Main.getLanguageManager();
        WaypointPlayer waypointPlayer = waypointManager.getPlayer(e.getPlayer().getUniqueId());

        //add if not in map
        if (waypointPlayer == null)
            waypointManager.addPlayer(e.getPlayer().getUniqueId());

        //if player is op, check for updates
        if (e.getPlayer().isOp()) {
            new UpdateChecker(plugin, 99866).getVersion(version -> {
                if (!plugin.getDescription().getVersion().equals(version)) {
                    e.getPlayer().sendMessage(ChatColor.AQUA + languageManager.getString("new-version-available") + "\n" +
                            ChatColor.YELLOW + languageManager.getString("current-version") + ": " + plugin.getDescription().getVersion() + "\n" +
                            languageManager.getString("updated-version") + ": " + version);
                    TextComponent textComponent = new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "" + net.md_5.bungee.api.ChatColor.UNDERLINE + languageManager.getString("click-to-download"));
                    textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/beaconwaypoints.99866/"));
                    e.getPlayer().spigot().sendMessage(textComponent);
                }
            });
        }
    }

    //add new non-activated waypoint when one is placed, and make the player who placed it the owner
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.BEACON) {
            UUID playerUUID = e.getPlayer().getUniqueId();
            Waypoint newWaypoint = new Waypoint(playerUUID, new WaypointCoord(e.getBlock().getLocation()));
            Main.getWaypointManager().addInactiveWaypoint(newWaypoint);
        }
    }

    //delete waypoint when beacon is broken
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.BEACON) {
            WaypointManager waypointManager = Main.getWaypointManager();
            LanguageManager languageManager = Main.getLanguageManager();
            WaypointCoord waypointCoord = new WaypointCoord(e.getBlock().getLocation());

            //check if player has permission to break waypoint beacons and if the beacon has waypoints
            Waypoint publicWaypoint1 = waypointManager.getPublicWaypoint(waypointCoord);
            List<Waypoint> privateWaypoints = waypointManager.getPrivateWaypointsAtCoord(waypointCoord);
            boolean ownsWaypoint = true;
            if (publicWaypoint1 != null && !publicWaypoint1.getOwnerUUID().equals(e.getPlayer().getUniqueId()))
                ownsWaypoint = false;
            if (ownsWaypoint && privateWaypoints.size() > 0) {
                for (Waypoint privateWaypoint : privateWaypoints) {
                    if (!privateWaypoint.getOwnerUUID().equals(e.getPlayer().getUniqueId())) {
                        ownsWaypoint = false;
                        break;
                    }
                }
            }
            if (!e.getPlayer().hasPermission("BeaconWaypoints.manageAllWaypoints") && !e.getPlayer().hasPermission("BeaconWaypoints.breakWaypointBeacons")) {
                FileConfiguration config = Main.getPlugin().getConfig();
                if (!config.contains("allow-beacon-break-by-owner"))
                    config.set("allow-beacon-break-by-owner", true);
                if (!(ownsWaypoint && config.getBoolean("allow-beacon-break-by-owner"))) {
                    e.getPlayer().sendMessage(ChatColor.RED + languageManager.getString("no-break-permission"));
                    e.setCancelled(true);
                }
            }
            else {
                //remove public/pinned waypoint
                Waypoint publicWaypoint = waypointManager.getPublicWaypoint(waypointCoord);
                if (publicWaypoint != null) {
                    waypointManager.removePublicWaypoint(waypointCoord);
                    e.getPlayer().sendMessage(ChatColor.RED + languageManager.getString("removed-public-waypoint") + " " + ChatColor.BOLD + publicWaypoint.getName());
                }

                //remove private waypoint
                for (WaypointPlayer waypointPlayer : waypointManager.getWaypointPlayers().values()) {
                    Waypoint privateWaypoint = waypointManager.getPrivateWaypoint(waypointPlayer.getUUID(), waypointCoord);
                    if (privateWaypoint != null) {
                        waypointManager.removePrivateWaypoint(waypointPlayer.getUUID(), waypointCoord);
                        Player player = Bukkit.getPlayer(waypointPlayer.getUUID());
                        if (player != null)
                            player.sendMessage(ChatColor.RED + languageManager.getString("removed-private-waypoint") + " " + ChatColor.BOLD + privateWaypoint.getName());
                    }
                }

                //remove inactive waypoint
                Waypoint inactiveWaypoint = waypointManager.getInactiveWaypoint(waypointCoord);
                if (inactiveWaypoint != null)
                    waypointManager.removeInactiveWaypoint(waypointCoord);
            }
        }
    }

    //deletes waypoints if they are removed with fill or setblock commands
    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent e) {
        if (e.getBlock().getType() == Material.AIR) {
            WaypointManager waypointManager = Main.getWaypointManager();
            WaypointCoord waypointCoord = new WaypointCoord(e.getBlock().getLocation());

            //remove public waypoint
            Waypoint publicWaypoint = waypointManager.getPublicWaypoint(waypointCoord);
            if (publicWaypoint != null) {
                waypointManager.removePublicWaypoint(waypointCoord);
            }

            //remove private waypoint
            for (WaypointPlayer waypointPlayer : waypointManager.getWaypointPlayers().values()) {
                Waypoint privateWaypoint = waypointManager.getPrivateWaypoint(waypointPlayer.getUUID(), waypointCoord);
                if (privateWaypoint != null)
                    waypointManager.removePrivateWaypoint(waypointPlayer.getUUID(), waypointCoord);
            }

            //remove inactive waypoint
            Waypoint inactiveWaypoint = waypointManager.getInactiveWaypoint(waypointCoord);
            if (inactiveWaypoint != null)
                waypointManager.removeInactiveWaypoint(waypointCoord);
        }
    }

    //when player opens a beacon
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND && Objects.requireNonNull(e.getClickedBlock()).getType() == Material.BEACON && !e.getPlayer().isSneaking()) {
            //check if player has permission to use waypoints
            Player player = e.getPlayer();
            if (player.hasPermission("BeaconWaypoints.useWaypoints")) {
                WaypointManager waypointManager = Main.getWaypointManager();
                WaypointCoord waypointCoord = new WaypointCoord(e.getClickedBlock().getLocation());
                Waypoint waypoint = waypointManager.getPinnedWaypoint(waypointCoord);
                if (waypoint == null)
                    waypoint = waypointManager.getPublicWaypoint(waypointCoord);
                if (waypoint == null)
                    waypoint = waypointManager.getPrivateWaypoint(player.getUniqueId(), waypointCoord);
                if (waypoint != null) {
                    e.setCancelled(true);
                    if (Main.getPlugin().getConfig().getBoolean("discovery-mode")) {
                        //discovery mode
                        if (!waypoint.playerDiscoveredWaypoint(player)) {
                            waypoint.addPlayerDiscovered(player);
                            player.sendMessage(ChatColor.GREEN + Main.getLanguageManager().getString("discovered-waypoint") + ": " + ChatColor.BOLD + waypoint.getName());
                        }
                    }
                    GUIs.beaconMenu(player, waypoint);
                }
            }
        }
    }

    //when player throws an ender pearl and is teleporting, cancel it
    @EventHandler
    public void onProjectileThrow(ProjectileLaunchEvent e) {
        Projectile projectile = e.getEntity();
        if (projectile.getShooter() instanceof Player && projectile.getType() == EntityType.ENDER_PEARL) {
            WaypointPlayer waypointPlayer = Main.getWaypointManager().getPlayer(((Player) projectile.getShooter()).getUniqueId());
            if (waypointPlayer != null && waypointPlayer.isTeleporting())
                e.setCancelled(true);
        }
    }

    //when player eats a chorus fruit and is teleporting, cancel it
    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        if (e.getItem().getType() == Material.CHORUS_FRUIT) {
            WaypointPlayer waypointPlayer = Main.getWaypointManager().getPlayer(e.getPlayer().getUniqueId());
            if (waypointPlayer != null && waypointPlayer.isTeleporting())
                e.setCancelled(true);
        }
    }

    //disable damage to player when teleporting
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            WaypointPlayer waypointPlayer = Main.getWaypointManager().getPlayer(e.getEntity().getUniqueId());
            if (waypointPlayer != null && waypointPlayer.isTeleporting())
                e.setCancelled(true);
        }
    }
}