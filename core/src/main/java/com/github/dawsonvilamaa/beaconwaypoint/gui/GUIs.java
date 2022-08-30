package com.github.dawsonvilamaa.beaconwaypoint.gui;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.Waypoint;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointCoord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GUIs {

    //shows all icons for waypoints
    public static void waypointIconPickerMenu(Player player, Waypoint waypoint, InventoryGUI previousGUI) {
        FileConfiguration config = Main.getPlugin().getConfig();

        MultiPageInventoryGUI gui = new MultiPageInventoryGUI(player, "Waypoint Icon", 5, previousGUI);

        //add waypoint icons
        if (!config.contains("waypoint-icons"))
            config.set("waypoint-icons", Waypoint.DEFAULT_WAYPOINT_ICONS);
        for (String iconStr : Main.getPlugin().getConfig().getStringList("waypoint-icons")) {
            try {
                Material icon = Material.valueOf(iconStr);
                InventoryGUIButton iconButton = new InventoryGUIButton(gui.getGUI(), null, null, icon);
                iconButton.setOnClick(e -> {
                    player.closeInventory();
                    waypoint.setIcon(icon);
                });
                gui.addButton(iconButton);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning(Main.getPlugin().getLanguageManager().getString("waypoint-icon-not-found") + " " + iconStr);
            }
        }

        gui.showMenu();
    }

    //menu that opens when player clicks on a beacon, giving a choice between regular beacon menu, public waypoints, or private waypoints
    public static void beaconMenu(Player player, Waypoint waypoint) {
        InventoryGUI gui = new InventoryGUI(player, "Waypoint: " + waypoint.getName(), 1, true);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 3);

        //vanilla beacon menu button
        InventoryGUIButton vanillaBeaconButton = new InventoryGUIButton(gui, ChatColor.RESET + Main.getPlugin().getLanguageManager().getString("change-beacon-effect"), null, Material.BEACON);
        vanillaBeaconButton.setOnClick(e -> {
            //opens the vanilla beacon menu using NMS, IDK how it managed to work, but I'll take it
            Block beacon = player.getWorld().getBlockAt(waypoint.getCoord().getLocation());
            Main.getVersionWrapper().openBeaconMenu(beacon, player);
        });
        gui.addButton(vanillaBeaconButton);

        boolean canUsePrivateWaypoints = player.hasPermission("BeaconWaypoints.usePrivateWaypoints");

        //public waypoints button
        InventoryGUIButton publicWaypointsButton = new InventoryGUIButton(gui, Main.getPlugin().getLanguageManager().getString("public-waypoints"), null, Material.FILLED_MAP);
        publicWaypointsButton.setOnClick(f -> {
            int beaconStatus = waypoint.getBeaconStatus();
            if (beaconStatus != 0)
                publicWaypointsMenu(player, waypoint, gui);
            else {
                player.sendMessage(ChatColor.RED + Main.getPlugin().getLanguageManager().getString("beacon-obstructed"));
                player.closeInventory();
            }
        });
        if (canUsePrivateWaypoints)
            gui.addButton(publicWaypointsButton);
        else {
            gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));
            gui.addButton(publicWaypointsButton);
        }

        //private waypoints button
        if (canUsePrivateWaypoints) {
            InventoryGUIButton privateWaypointsButton = new InventoryGUIButton(gui, Main.getPlugin().getLanguageManager().getString("private-waypoints"), null, Material.TRIPWIRE_HOOK);
            privateWaypointsButton.setOnClick(f -> {
                int beaconStatus = waypoint.getBeaconStatus();
                if (beaconStatus != 0)
                    privateWaypointsMenu(player, waypoint, gui);
                else {
                    player.sendMessage(ChatColor.RED + Main.getPlugin().getLanguageManager().getString("beacon-obstructed"));
                    player.closeInventory();
                }
            });
            gui.addButton(privateWaypointsButton);
        }

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 3);

        gui.showMenu();
    }

    //shows all public waypoints
    public static void publicWaypointsMenu(Player player, Waypoint waypoint, InventoryGUI previousGUI) {
        FileConfiguration config = Main.getPlugin().getConfig();

        int numRows = 0;
        if (!config.contains("public-waypoint-menu-rows"))
            config.set("public-waypoint-menu-rows", 3);
        else {
            numRows = config.getInt("public-waypoint-menu-rows");
            if (numRows <= 0)
                numRows = 1;
            else if (numRows > 5)
                numRows = 5;
        }

        MultiPageInventoryGUI gui = new MultiPageInventoryGUI(player, "Public Waypoints", numRows, previousGUI);

        //add buttons for all public waypoints
        for (Waypoint publicWaypoint : Main.getWaypointManager().getPublicWaypointsSortedAlphabetically()) {
            if (!publicWaypoint.getCoord().equals(waypoint.getCoord())) {
                WaypointCoord coord = publicWaypoint.getCoord();
                InventoryGUIButton waypointButton = new InventoryGUIButton(gui.getGUI(), publicWaypoint.getName(), ChatColor.GRAY + "" + coord.getX() + ", " + coord.getY() + ", " + coord.getZ() + "\n" + ChatColor.DARK_GRAY + Bukkit.getOfflinePlayer(publicWaypoint.getOwnerUUID()).getName() + "\n" + ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + publicWaypoint.getCoord().getWorldName(), publicWaypoint.getIcon());
                waypointButton.setOnClick(e -> {
                    player.closeInventory();
                    if (player.getLocation().distance(waypoint.getCoord().getLocation()) <= 5.5) {
                        if (Main.getWaypointManager().getPublicWaypoint(coord) == null)
                            player.sendMessage(ChatColor.RED + Main.getPlugin().getLanguageManager().getString("waypoint-does-not-exist"));
                        else if (publicWaypoint.getBeaconStatus() == 0)
                            player.sendMessage(ChatColor.RED + Main.getPlugin().getLanguageManager().getString("beacon-obstructed"));
                        else {
                            if (!config.contains("disable-group-teleporting"))
                                config.set("disable-group-teleporting", false);
                            if (Waypoint.checkPaymentRequirements(player, waypoint, publicWaypoint))
                                Waypoint.teleport(waypoint, publicWaypoint, config.getBoolean("disable-group-teleporting") ? player : null);
                        }
                    }
                });
                if (player.hasPermission("manageAllWaypoints") || publicWaypoint.getOwnerUUID().equals(player.getUniqueId())) {
                    waypointButton.setOnRightClick(e -> {
                        waypointOptionsMenu(player, publicWaypoint, waypoint, gui.getGUI(), true);
                    });
                }
                gui.addButton(waypointButton);
            }
        }

        gui.showMenu();

        //options button for this waypoint
        Waypoint thisWaypoint = Main.getWaypointManager().getPublicWaypoint(waypoint.getCoord());
        if (thisWaypoint != null && (waypoint.getOwnerUUID().equals(player.getUniqueId()) || player.hasPermission("manageAllWaypoints"))) {
            InventoryGUIButton optionsButton = new InventoryGUIButton(gui.getGUI(), Main.getPlugin().getLanguageManager().getString("public-waypoint-options"), null, thisWaypoint.getIcon());
            optionsButton.setOnClick(e -> {
                waypointOptionsMenu(player, thisWaypoint, waypoint, gui.getGUI(), true);
            });
            gui.getGUI().setButton(gui.getBottomRowSlot() + 8, optionsButton);
        }
    }

    //shows all private waypoints
    public static void privateWaypointsMenu(Player player, Waypoint waypoint, InventoryGUI previousGUI) {
        FileConfiguration config = Main.getPlugin().getConfig();

        int numRows = 0;
        if (!config.contains("private-waypoint-menu-rows"))
            config.set("private-waypoint-menu-rows", 2);
        else {
            numRows = config.getInt("private-waypoint-menu-rows");
            if (numRows <= 0)
                numRows = 1;
            else if (numRows > 5)
                numRows = 5;
        }

        MultiPageInventoryGUI gui = new MultiPageInventoryGUI(player, "Private Waypoints", numRows, previousGUI);

        //add buttons for all private waypoints
        for (Waypoint privateWaypoint : Main.getWaypointManager().getPrivateWaypointsSortedAlphabetically(player.getUniqueId())) {
            if (!privateWaypoint.getCoord().equals(waypoint.getCoord())) {
                WaypointCoord coord = privateWaypoint.getCoord();
                InventoryGUIButton waypointButton = new InventoryGUIButton(gui.getGUI(), privateWaypoint.getName(), ChatColor.GRAY + "" + coord.getX() + ", " + coord.getY() + ", " + coord.getZ() + "\n" + ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + privateWaypoint.getCoord().getWorldName(), privateWaypoint.getIcon());
                waypointButton.setOnClick(e -> {
                    player.closeInventory();
                    if (player.getLocation().distance(waypoint.getCoord().getLocation()) <= 5.5) {
                        if (Main.getWaypointManager().getPrivateWaypoint(player.getUniqueId(), coord) == null)
                            player.sendMessage(ChatColor.RED + Main.getPlugin().getLanguageManager().getString("waypoint-does-not-exist"));
                        else if (privateWaypoint.getBeaconStatus() == 0)
                            player.sendMessage(ChatColor.RED + Main.getPlugin().getLanguageManager().getString("beacon-obstructed"));
                        else {
                            if (!config.contains("disable-group-teleporting"))
                                config.set("disable-group-teleporting", false);
                            Waypoint.teleport(waypoint, privateWaypoint, config.getBoolean("disable-group-teleporting") ? player : null);
                        }
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
        Waypoint thisWaypoint = Main.getWaypointManager().getPrivateWaypoint(player.getUniqueId(), waypoint.getCoord());
        if (thisWaypoint != null && (waypoint.getOwnerUUID().equals(player.getUniqueId()) || player.hasPermission("manageAllWaypoints"))) {
            InventoryGUIButton optionsButton = new InventoryGUIButton(gui.getGUI(), Main.getPlugin().getLanguageManager().getString("private-waypoint-options"), null, thisWaypoint.getIcon());
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
        InventoryGUIButton backButton = new InventoryGUIButton(gui, Main.getPlugin().getLanguageManager().getString("back"), null, Material.PLAYER_HEAD);
        org.bukkit.inventory.ItemStack skull = new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner("MHF_ArrowLeft");
        skull.setItemMeta(skullMeta);
        backButton.setItem(skull);
        backButton.setName(ChatColor.WHITE + "Back");

        backButton.setOnClick(e -> {
            previousGUI.showMenu();
        });
        gui.addButton(backButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //change icon button
        InventoryGUIButton changeIconButton = new InventoryGUIButton(gui, Main.getPlugin().getLanguageManager().getString("change-icon"), null, Material.PAINTING);
        changeIconButton.setOnClick(e -> {
            waypointIconPickerMenu(player, selectedWaypoint, gui);
        });
        gui.addButton(changeIconButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //delete waypoint button
        InventoryGUIButton deleteButton = new InventoryGUIButton(gui, Main.getPlugin().getLanguageManager().getString("delete-waypoint"), null, Material.LAVA_BUCKET);
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
        InventoryGUIButton cancelButton = new InventoryGUIButton(gui, ChatColor.RED + Main.getPlugin().getLanguageManager().getString("cancel"), null, Material.RED_STAINED_GLASS_PANE);
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
        InventoryGUIButton confirmButton = new InventoryGUIButton(gui, Main.getPlugin().getLanguageManager().getString("confirm-delete"), null, Material.LIME_STAINED_GLASS_PANE);
        confirmButton.setOnClick(e -> {
            if (publicMenu)
                Main.getWaypointManager().removePublicWaypoint(selectedWaypoint.getCoord());
            else Main.getWaypointManager().removePrivateWaypoint(player.getUniqueId(), selectedWaypoint.getCoord());
            player.sendMessage(ChatColor.GREEN + (publicMenu ? Main.getPlugin().getLanguageManager().getString("removed-public-waypoint") : Main.getPlugin().getLanguageManager().getString("removed-private-waypoint")) + " " + ChatColor.BOLD + selectedWaypoint.getName());
            player.closeInventory();
        });
        gui.addButton(confirmButton);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);

        gui.showMenu();
    }
}