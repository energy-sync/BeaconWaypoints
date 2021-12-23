package com.github.dawsonvilamaa.beaconwaypoint.listeners;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.gui.InventoryGUI;
import com.github.dawsonvilamaa.beaconwaypoint.gui.InventoryGUIButton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryListener implements Listener {
    Main plugin;

    /**
     * @param plugin
     */
    public InventoryListener(Main plugin) {
        this.plugin = plugin;
    }

    //prevent players from taking items in GUI menus
    @EventHandler
    public void onItemClick(InventoryClickEvent e) {
        InventoryGUI gui = Main.menuManager.getMenuByPlayerUUID(e.getWhoClicked().getUniqueId());
        if (gui != null && e.getWhoClicked().equals(gui.getPlayer()) && e.getCurrentItem() != null && e.getView().getTitle().equals(gui.getName())) {
            InventoryGUIButton button = gui.getButtons().get(e.getRawSlot());
            if (button != null) {
                if (button.getOnClick() != null)
                    button.onClick(e);
                if (button.isLocked())
                    e.setCancelled(true);
            }
            else if (gui.isLocked())
                e.setCancelled(true);
        }
    }

    //remove all click events on buttons when the menu is closed
    @EventHandler
    public void onMenuClose(InventoryCloseEvent e) {
        InventoryGUI gui = Main.menuManager.getMenuByPlayerUUID(e.getPlayer().getUniqueId());
        if (gui != null && e.getView().getTitle().equals(gui.getName()) && e.getPlayer().equals(gui.getPlayer())) {
            Main.menuManager.removeMenu(e.getPlayer().getUniqueId());
        }
    }
}
