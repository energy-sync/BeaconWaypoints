package com.github.dawsonvilamaa.beaconwaypoint.gui;

import com.github.dawsonvilamaa.beaconwaypoint.LanguageManager;
import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.Waypoint;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointCoord;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointHelper;
import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collection;

public class GUIs {

    //shows all icons for waypoints
    public static void waypointIconPickerMenu(Player player, Waypoint waypoint, InventoryGUI previousGUI) {
        FileConfiguration config = Main.getPlugin().getConfig();
        LanguageManager languageManager = Main.getLanguageManager();

        MultiPageInventoryGUI gui = new MultiPageInventoryGUI(player, languageManager.getString("waypoint-icon"), 5, previousGUI);

        //add waypoint icons
        if (!config.contains("waypoint-icons"))
            config.set("waypoint-icons", WaypointHelper.DEFAULT_WAYPOINT_ICONS);
        for (String iconStr : config.getStringList("waypoint-icons")) {
            try {
                Material icon = Material.valueOf(iconStr);
                InventoryGUIButton iconButton = new InventoryGUIButton(gui.getGUI(), null, null, icon);
                iconButton.setOnClick(e -> {
                    player.closeInventory();
                    waypoint.setIcon(icon);
                });
                gui.addButton(iconButton);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning(languageManager.getString("waypoint-icon-not-found") + " " + iconStr);
            }
        }

        gui.showMenu();
    }

    //menu that opens when player clicks on a beacon, giving a choice between regular beacon menu, public waypoints, or private waypoints
    public static void beaconMenu(Player player, Waypoint waypoint) {
        LanguageManager languageManager = Main.getLanguageManager();

        InventoryGUI gui = new InventoryGUI(player, languageManager.getString("waypoint") + ": " + waypoint.getName(), 1, true);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 3);

        //vanilla beacon menu button
        InventoryGUIButton vanillaBeaconButton = new InventoryGUIButton(gui, ChatColor.RESET + languageManager.getString("change-beacon-effect"), null, Material.BEACON);
        vanillaBeaconButton.setOnClick(e -> {
            //opens the vanilla beacon menu using NMS, IDK how it managed to work, but I'll take it
            Block beacon = player.getWorld().getBlockAt(waypoint.getCoord().getLocation());
            Main.getVersionWrapper().openBeaconMenu(beacon, player);
        });
        gui.addButton(vanillaBeaconButton);

        boolean canUsePrivateWaypoints = player.hasPermission("BeaconWaypoints.usePrivateWaypoints");

        //public waypoints button
        InventoryGUIButton publicWaypointsButton = new InventoryGUIButton(gui, languageManager.getString("public-waypoints"), null, Material.FILLED_MAP);
        publicWaypointsButton.setOnClick(f -> {
            int beaconStatus = waypoint.getBeaconStatus();
            if (beaconStatus != 0)
                publicWaypointsMenu(player, waypoint, gui);
            else {
                player.sendMessage(ChatColor.RED + languageManager.getString("beacon-obstructed"));
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
            InventoryGUIButton privateWaypointsButton = new InventoryGUIButton(gui, languageManager.getString("private-waypoints"), null, Material.TRIPWIRE_HOOK);
            privateWaypointsButton.setOnClick(f -> {
                int beaconStatus = waypoint.getBeaconStatus();
                if (beaconStatus != 0)
                    privateWaypointsMenu(player, waypoint, gui);
                else {
                    player.sendMessage(ChatColor.RED + languageManager.getString("beacon-obstructed"));
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
        WaypointManager waypointManager = Main.getWaypointManager();
        LanguageManager languageManager = Main.getLanguageManager();

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

        MultiPageInventoryGUI gui = new MultiPageInventoryGUI(player, languageManager.getString("public-waypoints"), numRows, previousGUI);

        //add buttons for all public waypoints
        boolean discoveryModeEnabled = config.getBoolean("discovery-mode");
        Collection<Waypoint> allPublicWaypoints = waypointManager.getPinnedWaypointsSortedAlphabetically();
        allPublicWaypoints.addAll(waypointManager.getPublicWaypointsSortedAlphabetically());
        for (Waypoint publicWaypoint : allPublicWaypoints) {
            if ((!discoveryModeEnabled || publicWaypoint.playerDiscoveredWaypoint(player)) && !publicWaypoint.getCoord().equals(waypoint.getCoord())) {
                WaypointCoord coord = publicWaypoint.getCoord();
                String waypointName = publicWaypoint.getName();
                if (publicWaypoint.isPinned())
                    waypointName += " " + ChatColor.GREEN + "(" + languageManager.getString("pinned") + ")";
                InventoryGUIButton waypointButton = new InventoryGUIButton(gui.getGUI(), waypointName, WaypointHelper.getWaypointDescription(waypoint, publicWaypoint), publicWaypoint.getIcon());
                waypointButton.setOnClick(e -> {
                    player.closeInventory();
                    if (player.getLocation().distance(waypoint.getCoord().getLocation()) <= 5.5) {
                        if (waypointManager.getPublicWaypoint(coord) == null)
                            player.sendMessage(ChatColor.RED + languageManager.getString("waypoint-does-not-exist"));
                        else if (publicWaypoint.getBeaconStatus() == 0)
                            player.sendMessage(ChatColor.RED + languageManager.getString("beacon-obstructed"));
                        else {
                            int costPerChunk;
                            String paymentMode = config.getString("payment-mode");
                            if (paymentMode != null) {
                                if (paymentMode.equals("xp"))
                                    costPerChunk = config.getInt("xp-cost-per-chunk");
                                else if (paymentMode.equals("money"))
                                    costPerChunk = config.getInt("money-cost-per-chunk");
                                else costPerChunk = 0;
                            }
                            else {
                                costPerChunk = 0;
                                paymentMode = "none";
                            }
                            if (WaypointHelper.checkPaymentRequirements(player, waypoint, publicWaypoint, WaypointHelper.calculateCost(waypoint, publicWaypoint, paymentMode, costPerChunk, config.getDouble("cost-multiplier"))))
                                WaypointHelper.teleport(waypoint, publicWaypoint, player, config.getBoolean("disable-group-teleporting"));
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
        Waypoint thisWaypoint = waypointManager.getPublicWaypoint(waypoint.getCoord());
        if (thisWaypoint != null && (waypoint.getOwnerUUID().equals(player.getUniqueId()) || player.hasPermission("manageAllWaypoints"))) {
            InventoryGUIButton optionsButton = new InventoryGUIButton(gui.getGUI(), languageManager.getString("public-waypoint-options"), null, thisWaypoint.getIcon());
            optionsButton.setOnClick(e -> {
                waypointOptionsMenu(player, thisWaypoint, waypoint, gui.getGUI(), true);
            });
            gui.getGUI().setButton(gui.getBottomRowSlot() + 8, optionsButton);
        }
    }

    //shows all private waypoints
    public static void privateWaypointsMenu(Player player, Waypoint waypoint, InventoryGUI previousGUI) {
        FileConfiguration config = Main.getPlugin().getConfig();
        WaypointManager waypointManager = Main.getWaypointManager();
        LanguageManager languageManager = Main.getLanguageManager();

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

        MultiPageInventoryGUI gui = new MultiPageInventoryGUI(player, languageManager.getString("private-waypoints"), numRows, previousGUI);

        //add buttons for all private waypoints
        for (Waypoint privateWaypoint : waypointManager.getPrivateWaypointsSortedAlphabetically(player.getUniqueId())) {
            if (!privateWaypoint.getCoord().equals(waypoint.getCoord())) {
                WaypointCoord coord = privateWaypoint.getCoord();
                InventoryGUIButton waypointButton = new InventoryGUIButton(gui.getGUI(), privateWaypoint.getName(), WaypointHelper.getWaypointDescription(waypoint, privateWaypoint), privateWaypoint.getIcon());
                waypointButton.setOnClick(e -> {
                    player.closeInventory();
                    if (player.getLocation().distance(waypoint.getCoord().getLocation()) <= 5.5) {
                        if (waypointManager.getPrivateWaypoint(player.getUniqueId(), coord) == null)
                            player.sendMessage(ChatColor.RED + languageManager.getString("waypoint-does-not-exist"));
                        else if (privateWaypoint.getBeaconStatus() == 0)
                            player.sendMessage(ChatColor.RED + languageManager.getString("beacon-obstructed"));
                        else {
                            int costPerChunk;
                            String paymentMode = config.getString("payment-mode");
                            if (paymentMode != null) {
                                if (paymentMode.equals("xp"))
                                    costPerChunk = config.getInt("xp-cost-per-chunk");
                                else if (paymentMode.equals("money"))
                                    costPerChunk = config.getInt("money-cost-per-chunk");
                                else costPerChunk = 0;
                            }
                            else {
                                costPerChunk = 0;
                                paymentMode = "none";
                            }
                            if (WaypointHelper.checkPaymentRequirements(player, waypoint, privateWaypoint, WaypointHelper.calculateCost(waypoint, privateWaypoint, paymentMode, costPerChunk, config.getDouble("cost-multiplier"))))
                                WaypointHelper.teleport(waypoint, privateWaypoint, player, config.getBoolean("disable-group-teleporting"));
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
        Waypoint thisWaypoint = waypointManager.getPrivateWaypoint(player.getUniqueId(), waypoint.getCoord());
        if (thisWaypoint != null && (waypoint.getOwnerUUID().equals(player.getUniqueId()) || player.hasPermission("manageAllWaypoints"))) {
            InventoryGUIButton optionsButton = new InventoryGUIButton(gui.getGUI(), languageManager.getString("private-waypoint-options"), null, thisWaypoint.getIcon());
            optionsButton.setOnClick(e -> {
                waypointOptionsMenu(player, thisWaypoint, waypoint, gui.getGUI(), false);
            });
            gui.getGUI().setButton(gui.getBottomRowSlot() + 8, optionsButton);
        }
    }

    //right click options menu for waypoint
    public static void waypointOptionsMenu(Player player, Waypoint selectedWaypoint, Waypoint originalWaypoint, InventoryGUI previousGUI, boolean publicMenu) {
        LanguageManager languageManager = Main.getLanguageManager();

        InventoryGUI gui = new InventoryGUI(player, languageManager.getString("options") + ": " + selectedWaypoint.getName(), 1, true);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);

        //back button
        InventoryGUIButton backButton = GUIs.createHeadButton(gui, languageManager.getString("back"), ChatColor.DARK_GRAY + previousGUI.getTitle(), "MHF_ArrowLeft");
        backButton.setOnClick(e -> {
            previousGUI.showMenu();
        });
        gui.addButton(backButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //change icon button
        InventoryGUIButton changeIconButton = new InventoryGUIButton(gui, languageManager.getString("change-icon"), null, Material.PAINTING);
        changeIconButton.setOnClick(e -> {
            waypointIconPickerMenu(player, selectedWaypoint, gui);
        });
        gui.addButton(changeIconButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //delete waypoint button
        InventoryGUIButton deleteButton = new InventoryGUIButton(gui, languageManager.getString("delete-waypoint"), null, Material.LAVA_BUCKET);
        deleteButton.setOnClick(e -> {
            confirmDeleteWaypointMenu(player, selectedWaypoint, originalWaypoint, gui, publicMenu);
        });
        gui.addButton(deleteButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //pin/unpin button
        if (publicMenu && player.hasPermission("BeaconWaypoints.manageAllWaypoints")) {
            WaypointManager waypointManager = Main.getWaypointManager();
            InventoryGUIButton pinButton = new InventoryGUIButton(gui, selectedWaypoint.isPinned() ? languageManager.getString("unpin-waypoint") : languageManager.getString("pin-waypoint"), null, Material.NETHER_STAR);
            pinButton.setOnClick(e -> {
                if (selectedWaypoint.isPinned()) {
                    waypointManager.unpinWaypoint(selectedWaypoint);
                    pinButton.setName(languageManager.getString("pin-waypoint"));
                }
                else {
                    waypointManager.pinWaypoint(selectedWaypoint);
                    pinButton.setName(languageManager.getString("unpin-waypoint"));
                }
            });
            gui.addButton(pinButton);
        }
        else gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        gui.showMenu();
    }

    //confirms deletion of a waypoint
    public static void confirmDeleteWaypointMenu(Player player, Waypoint selectedWaypoint, Waypoint originalWaypoint, InventoryGUI previousGUI, boolean publicMenu) {
        WaypointManager waypointManager = Main.getWaypointManager();
        LanguageManager languageManager = Main.getLanguageManager();

        InventoryGUI gui = new InventoryGUI(player, languageManager.getString("confirm-delete"), 1, true);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);

        //cancel button
        InventoryGUIButton cancelButton = new InventoryGUIButton(gui, ChatColor.RED + languageManager.getString("cancel"), null, Material.RED_STAINED_GLASS_PANE);
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
        InventoryGUIButton confirmButton = new InventoryGUIButton(gui, languageManager.getString("confirm-delete"), null, Material.LIME_STAINED_GLASS_PANE);
        confirmButton.setOnClick(e -> {
            if (publicMenu)
                waypointManager.removePublicWaypoint(selectedWaypoint.getCoord());
            else waypointManager.removePrivateWaypoint(player.getUniqueId(), selectedWaypoint.getCoord());
            player.sendMessage(ChatColor.GREEN + (publicMenu ? languageManager.getString("removed-public-waypoint") : languageManager.getString("removed-private-waypoint")) + " " + ChatColor.BOLD + selectedWaypoint.getName());
            player.closeInventory();
        });
        gui.addButton(confirmButton);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);

        gui.showMenu();
    }

    //creates an inventory GUI button with a player's head
    public static InventoryGUIButton createHeadButton(InventoryGUI gui, String name, String description, String playerName) {
        InventoryGUIButton button = new InventoryGUIButton(gui, name, description, Material.PLAYER_HEAD);
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
        skull.setItemMeta(skullMeta);
        button.setItem(skull);
        button.setName(name);
        button.setDescription(description);
        return button;
    }
}