package com.github.dawsonvilamaa.beaconwaypoint.listeners;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.gui.InventoryGUI;
import com.github.dawsonvilamaa.beaconwaypoint.gui.InventoryGUIButton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

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
        InventoryGUI gui = Main.getMenuManager().getMenuByPlayerUUID(e.getWhoClicked().getUniqueId());
        if (gui != null && e.getWhoClicked().equals(gui.getPlayer()) && e.getCurrentItem() != null && e.getView().getTitle().equals(gui.getName())) {
            InventoryGUIButton button = gui.getButtons().get(e.getRawSlot());
            if (button != null) {
                if (button.getOnClick() != null) {
                    if (e.getClick() == ClickType.LEFT)
                        button.onClick(e);
                    else if (e.getClick() == ClickType.RIGHT)
                        button.onRightClick(e);
                }
                if (button.isLocked())
                    e.setCancelled(true);
            }
            else if (gui.isLocked())
                e.setCancelled(true);
        }
    }
}