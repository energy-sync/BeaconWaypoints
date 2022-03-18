package com.github.dawsonvilamaa.beaconwaypoint.gui;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.Waypoint;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointCoord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Objects;
import java.util.UUID;

public class GUIs {

    //shows all icons for waypoints
    public static void waypointIconPickerMenu(Player player, Waypoint waypoint, InventoryGUI previousGUI) {
        MultiPageInventoryGUI gui = new MultiPageInventoryGUI(player, "Waypoint Icon", 5, previousGUI);

        //add waypoint icons
        for (String iconStr : Main.plugin.getConfig().getStringList("waypoint-icons")) {
            try {
                Material icon = Material.valueOf(iconStr);
                InventoryGUIButton iconButton = new InventoryGUIButton(gui.getGUI(), null, null, icon);
                iconButton.setOnClick(e -> {
                    player.closeInventory();
                    waypoint.setIcon(icon);
                });
                gui.addButton(iconButton);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Could not add waypoint icon. No item found for " + iconStr);
            }
        }

        gui.showMenu();
    }

    //menu that opens when player clicks on a beacon, giving a choice between regular beacon menu, public waypoints, or private waypoints
    public static void beaconMenu(Player player, Waypoint waypoint) {
        InventoryGUI gui = new InventoryGUI(player, "Waypoint: " + waypoint.getName(), 1, true);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 3);

        //vanilla beacon menu button
        InventoryGUIButton vanillaBeaconButton = new InventoryGUIButton(gui, ChatColor.RESET + "Change beacon effect", null, Material.BEACON);
        vanillaBeaconButton.setOnClick(e -> {
            //opens the vanilla beacon menu using NMS, IDK how it managed to work, but I'll take it
            Block beacon = player.getWorld().getBlockAt(waypoint.getCoord().getLocation());
            Main.version.openBeaconMenu(beacon, player);
        });
        gui.addButton(vanillaBeaconButton);

        //public waypoints button
        InventoryGUIButton publicWaypointsButton = new InventoryGUIButton(gui, "Public Waypoints", null, Material.FILLED_MAP);
        publicWaypointsButton.setOnClick(f -> publicWaypointsMenu(player, waypoint, gui));
        gui.addButton(publicWaypointsButton);

        //private waypoints button
        InventoryGUIButton privateWaypointsButton = new InventoryGUIButton(gui, "Private Waypoints", null, Material.TRIPWIRE_HOOK);
        privateWaypointsButton.setOnClick(f -> privateWaypointsMenu(player, waypoint, gui));
        gui.addButton(privateWaypointsButton);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 3);

        gui.showMenu();
    }

    //shows all public waypoints
    public static void publicWaypointsMenu(Player player, Waypoint waypoint, InventoryGUI previousGUI) {
        int numRows = Main.plugin.getConfig().getInt("public-waypoint-menu-rows");
        if (numRows <= 0)
            numRows = 1;
        else if (numRows > 5)
            numRows = 5;
        MultiPageInventoryGUI gui = new MultiPageInventoryGUI(player, "Public Waypoints", numRows, previousGUI);

        //add buttons for all public waypoints
        for (Waypoint publicWaypoint : Main.waypointManager.getPublicWaypointsSortedAlphabetically()) {
            if (!publicWaypoint.getCoord().equals(waypoint.getCoord())) {
                WaypointCoord coord = publicWaypoint.getCoord();
                InventoryGUIButton waypointButton = new InventoryGUIButton(gui.getGUI(), publicWaypoint.getName(), ChatColor.GRAY + "" + coord.getX() + ", " + coord.getY() + ", " + coord.getZ() + "\n" + ChatColor.DARK_GRAY + Bukkit.getOfflinePlayer(publicWaypoint.getOwnerUUID()).getName() + "\n" + ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + publicWaypoint.getCoord().getWorldName(), publicWaypoint.getIcon());
                waypointButton.setOnClick(e -> {
                    player.closeInventory();
                    if (player.getLocation().distance(waypoint.getCoord().getLocation()) <= 5.5) {
                        if (Main.waypointManager.getPublicWaypoint(coord) == null)
                            player.sendMessage(ChatColor.RED + "That waypoint doesn't exist!");
                        else if (publicWaypoint.getBeaconStatus() == 0)
                            player.sendMessage(ChatColor.RED + "The destination beacon is not able to be traveled to. It either is not constructed correctly, or something is obstructing the beam.");
                        else Waypoint.teleport(waypoint, publicWaypoint, Main.plugin.getConfig().getBoolean("disable-group-teleporting") ? player : null);
                    }
                });
                if (publicWaypoint.getOwnerUUID().equals(player.getUniqueId())) {
                    waypointButton.setOnRightClick(e -> {
                        waypointOptionsMenu(player, publicWaypoint, waypoint, gui.getGUI(), true);
                    });
                }
                gui.addButton(waypointButton);
            }
        }

        gui.showMenu();

        //options button for this waypoint
        Waypoint thisWaypoint = Main.waypointManager.getPublicWaypoint(waypoint.getCoord());
        if (thisWaypoint != null) {
            InventoryGUIButton optionsButton = new InventoryGUIButton(gui.getGUI(), "Options for this public waypoint", null, thisWaypoint.getIcon());
            optionsButton.setOnClick(e -> {
                waypointOptionsMenu(player, thisWaypoint, waypoint, gui.getGUI(), true);
            });
            gui.getGUI().setButton(gui.getBottomRowSlot() + 8, optionsButton);
        }
    }

    //shows all private waypoints
    public static void privateWaypointsMenu(Player player, Waypoint waypoint, InventoryGUI previousGUI) {
        int numRows = Main.plugin.getConfig().getInt("private-waypoint-menu-rows");
        if (numRows <= 0)
            numRows = 1;
        else if (numRows > 5)
            numRows = 5;
        MultiPageInventoryGUI gui = new MultiPageInventoryGUI(player, "Private Waypoints", numRows, previousGUI);

        //add buttons for all private waypoints
        for (Waypoint privateWaypoint : Main.waypointManager.getPrivateWaypointsSortedAlphabetically(player.getUniqueId())) {
            if (!privateWaypoint.getCoord().equals(waypoint.getCoord())) {
                WaypointCoord coord = privateWaypoint.getCoord();
                InventoryGUIButton waypointButton = new InventoryGUIButton(gui.getGUI(), privateWaypoint.getName(), ChatColor.GRAY + "" + coord.getX() + ", " + coord.getY() + ", " + coord.getZ() + "\n" + ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + privateWaypoint.getCoord().getWorldName(), privateWaypoint.getIcon());
                waypointButton.setOnClick(e -> {
                    player.closeInventory();
                    if (player.getLocation().distance(waypoint.getCoord().getLocation()) <= 5.5) {
                        if (Main.waypointManager.getPrivateWaypoint(player.getUniqueId(), coord) == null)
                            player.sendMessage(ChatColor.RED + "That waypoint doesn't exist!");
                        else if (privateWaypoint.getBeaconStatus() == 0)
                            player.sendMessage(ChatColor.RED + "The destination beacon is not able to be traveled to. It either is not constructed correctly, or something is obstructing the beam.");
                        else Waypoint.teleport(waypoint, privateWaypoint, Main.plugin.getConfig().getBoolean("disable-group-teleporting") ? player : null);
                    }
                });
                if (privateWaypoint.getOwnerUUID().equals(player.getUniqueId())) {
                    waypointButton.setOnRightClick(e -> {
                        waypointOptionsMenu(player, privateWaypoint, waypoint, gui.getGUI(), false);
                    });
                }
                gui.addButton(waypointButton);
            }
        }

        gui.showMenu();

        //options button for this waypoint
        Waypoint thisWaypoint = Main.waypointManager.getPrivateWaypoint(player.getUniqueId(), waypoint.getCoord());
        if (thisWaypoint != null) {
            InventoryGUIButton optionsButton = new InventoryGUIButton(gui.getGUI(), "Options for this private waypoint", null, thisWaypoint.getIcon());
            optionsButton.setOnClick(e -> {
                waypointOptionsMenu(player, thisWaypoint, waypoint, gui.getGUI(), false);
            });
            gui.getGUI().setButton(gui.getBottomRowSlot() + 8, optionsButton);
        }
    }

    //right click options menu for waypoint
    public static void waypointOptionsMenu(Player player, Waypoint selectedWaypoint, Waypoint originalWaypoint, InventoryGUI previousGUI, boolean publicMenu) {
        InventoryGUI gui = new InventoryGUI(player, "Options: " + selectedWaypoint.getName(), 1, true);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);

        //back button
        InventoryGUIButton backButton = new InventoryGUIButton(gui, "Back", null, Material.PLAYER_HEAD);
        org.bukkit.inventory.ItemStack skull = new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        Objects.requireNonNull(skullMeta).setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString("a68f0b64-8d14-4000-a95f-4b9ba14f8df9")));
        skull.setItemMeta(skullMeta);
        backButton.setItem(skull);
        backButton.setName(ChatColor.WHITE + "Back");

        backButton.setOnClick(e -> {
            previousGUI.showMenu();
        });
        gui.addButton(backButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //change icon button
        InventoryGUIButton changeIconButton = new InventoryGUIButton(gui, "Change Icon", null, Material.PAINTING);
        changeIconButton.setOnClick(e -> {
            waypointIconPickerMenu(player, selectedWaypoint, gui);
        });
        gui.addButton(changeIconButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //delete waypoint button
        InventoryGUIButton deleteButton = new InventoryGUIButton(gui, "Delete Waypoint", null, Material.LAVA_BUCKET);
        deleteButton.setOnClick(e -> {
            confirmDeleteWaypointMenu(player, selectedWaypoint, originalWaypoint, gui, publicMenu);
        });
        gui.addButton(deleteButton);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);

        gui.showMenu();
    }

    //confirms deletion of a waypoint
    public static void confirmDeleteWaypointMenu(Player player, Waypoint selectedWaypoint, Waypoint originalWaypoint, InventoryGUI previousGUI, boolean publicMenu) {
        InventoryGUI gui = new InventoryGUI(player, "Confirm Waypoint Delete", 1, true);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);

        //cancel button
        InventoryGUIButton cancelButton = new InventoryGUIButton(gui, ChatColor.RED + "Cancel", null, Material.RED_STAINED_GLASS_PANE);
        cancelButton.setOnClick(e -> {
            previousGUI.showMenu();
        });
        gui.addButton(cancelButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //waypoint icon
        WaypointCoord coord = selectedWaypoint.getCoord();
        gui.addButton(new InventoryGUIButton(gui, selectedWaypoint.getName(), ChatColor.GRAY + "" + coord.getX() + ", " + coord.getY() + ", " + coord.getZ() + "\n" + ChatColor.DARK_GRAY + Bukkit.getOfflinePlayer(selectedWaypoint.getOwnerUUID()).getName(), selectedWaypoint.getIcon()));

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //confirm button
        InventoryGUIButton confirmButton = new InventoryGUIButton(gui, "Confirm Delete", null, Material.LIME_STAINED_GLASS_PANE);
        confirmButton.setOnClick(e -> {
            if (publicMenu)
                Main.waypointManager.removePublicWaypoint(selectedWaypoint.getCoord());
            else Main.waypointManager.removePrivateWaypoint(player.getUniqueId(), selectedWaypoint.getCoord());
            player.sendMessage(ChatColor.GREEN + "Removed " + (publicMenu ? "public" : "private") + " waypoint " + ChatColor.BOLD + selectedWaypoint.getName());
            player.closeInventory();
        });
        gui.addButton(confirmButton);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);

        gui.showMenu();
    }
}
