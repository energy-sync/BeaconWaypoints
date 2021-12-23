package com.github.dawsonvilamaa.beaconwaypoint.gui;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.Waypoint;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointCoord;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class GUIs {

    //shows all icons for waypoints
    public static void waypointIconPickerMenu(Player player, Waypoint waypoint) {
        Material icons[] = {
                Material.APPLE,             Material.SHROOMLIGHT,  Material.TOTEM_OF_UNDYING, Material.EMERALD,      Material.DIAMOND,           Material.END_CRYSTAL,           Material.LEATHER,          Material.FILLED_MAP,      Material.SNOW_BLOCK,
                Material.RED_MUSHROOM,      Material.CARROT,       Material.GOLDEN_APPLE,     Material.CREEPER_HEAD, Material.PRISMARINE_BRICKS, Material.ALLIUM,                Material.IRON_PICKAXE,     Material.QUARTZ_BRICKS,   Material.SKELETON_SKULL,
                Material.POPPY,             Material.PUMPKIN,      Material.HONEYCOMB,        Material.SEA_LANTERN,  Material.BLUE_ICE,          Material.PURPUR_BLOCK,          Material.ENCHANTING_TABLE, Material.OAK_LOG,         Material.WHEAT,
                Material.RED_BED,           Material.ORANGE_TULIP, Material.BLAZE_POWDER,     Material.SUGAR_CANE,   Material.LAPIS_LAZULI,      Material.CHORUS_FRUIT,          Material.END_PORTAL_FRAME, Material.ELYTRA,          Material.BREWING_STAND,
                Material.REDSTONE,          Material.RED_SAND,     Material.END_STONE,        Material.CACTUS,       Material.WATER_BUCKET,      Material.SHULKER_BOX,           Material.CHEST,            Material.NETHERITE_INGOT, Material.SOUL_SAND,
                Material.RED_NETHER_BRICKS, Material.MAGMA_BLOCK,  Material.SAND,             Material.ENDER_PEARL,  Material.WARPED_STEM,       Material.STRIPPED_CRIMSON_STEM, Material.ZOMBIE_HEAD,      Material.OBSIDIAN,        Material.WITHER_SKELETON_SKULL
        };

        InventoryGUI gui = new InventoryGUI(player, "Waypoint Icon", 6, true);

        for (int iconIndex = 0; iconIndex < icons.length; iconIndex++) {
            InventoryGUIButton iconButton = new InventoryGUIButton(gui, null, null, icons[iconIndex]);
            int finalIconIndex = iconIndex;
            iconButton.setOnClick(f -> {
                player.closeInventory();
                waypoint.setIcon(icons[finalIconIndex]);
            });
            gui.addButton(iconButton);
        }

        gui.showMenu();
    }

    //menu that opens when player clicks on a beacon, giving a choice between regular beacon menu, public waypoints, or private waypoints
    public static void privateOrPublicMenu(Player player, Waypoint waypoint, PlayerInteractEvent e) {
        InventoryGUI gui = new InventoryGUI(player, waypoint.getName() + " Waypoint", 1, true);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 3);

        InventoryGUIButton publicWaypointsButton = new InventoryGUIButton(gui, "Public Waypoints", null, Material.FILLED_MAP);
        publicWaypointsButton.setOnClick(f -> {
            publicWaypointsMenu(player, waypoint, e);
        });
        gui.addButton(publicWaypointsButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        InventoryGUIButton privateWaypointsButton = new InventoryGUIButton(gui, "Private Waypoints", null, Material.TRIPWIRE_HOOK);
        privateWaypointsButton.setOnClick(f -> {
            privateWaypointsMenu(player, waypoint, e);
        });
        gui.addButton(privateWaypointsButton);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 3);

        gui.showMenu();
    }

    //shows all public waypoints
    public static void publicWaypointsMenu(Player player, Waypoint waypoint, PlayerInteractEvent e) {
        InventoryGUI gui = new InventoryGUI(player, "Public Waypoints", 6, true);

        //add buttons for each public waypoint
        for (Waypoint publicWaypoint : Main.waypointManager.getPublicWaypointsSortedAlphabetically()) {
            if (!publicWaypoint.equals(waypoint)) {
                InventoryGUIButton waypointButton = new InventoryGUIButton(gui, publicWaypoint.getName(), null, waypoint.getIcon());
                waypointButton.setOnClick(f -> {
                    player.closeInventory();
                    WaypointCoord coord = publicWaypoint.getCoord();
                    Location tpLoc = new Location(Bukkit.getWorld(coord.getWorldName()), coord.getX(), coord.getY(), coord.getZ());
                    tpLoc.setX(tpLoc.getX() + 0.5);
                    tpLoc.setY(tpLoc.getY() + 1);
                    tpLoc.setZ(tpLoc.getZ() + 0.5);
                    tpLoc.setDirection(player.getLocation().getDirection());
                    player.teleport(tpLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                });
                gui.addButton(waypointButton);
            }
        }

        gui.showMenu();
    }

    //shows all private waypoints
    public static void privateWaypointsMenu(Player player, Waypoint waypoint, PlayerInteractEvent e) {
        InventoryGUI gui = new InventoryGUI(player, "Private Waypoints", 3, true);

        //add buttons for each private waypoint
        for (Waypoint privateWaypoint : Main.waypointManager.getPrivateWaypointsSortedAlphabetically(player.getUniqueId())) {
            if (!privateWaypoint.equals(waypoint)) {
                InventoryGUIButton waypointButton = new InventoryGUIButton(gui, privateWaypoint.getName(), null, waypoint.getIcon());
                waypointButton.setOnClick(f -> {
                    player.closeInventory();
                    WaypointCoord coord = privateWaypoint.getCoord();
                    Location tpLoc = new Location(Bukkit.getWorld(coord.getWorldName()), coord.getX(), coord.getY(), coord.getZ());
                    tpLoc.setX(tpLoc.getX() + 0.5);
                    tpLoc.setY(tpLoc.getY() + 1);
                    tpLoc.setZ(tpLoc.getZ() + 0.5);
                    tpLoc.setDirection(player.getLocation().getDirection());
                    player.teleport(tpLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                });
                gui.addButton(waypointButton);
            }
        }

        gui.showMenu();
    }
}
