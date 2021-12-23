package com.github.dawsonvilamaa.beaconwaypoint.gui;

import java.util.HashMap;
import java.util.UUID;

public class MenuManager {
    private HashMap<UUID, InventoryGUI> menus;

    public MenuManager() {
        menus = new HashMap<>();
    }

    /**
     * @param uuid
     * @param menu
     */
    public void addMenu(UUID uuid, InventoryGUI menu) {
        menus.put(uuid, menu);
    }

    /**
     * @param uuid
     * @return menu
     */
    public InventoryGUI getMenu(UUID uuid) {
        return menus.get(uuid);
    }

    /**
     * @param uuid
     */
    public void removeMenu(UUID uuid) {
        menus.remove(uuid);
    }

    /**
     * @param uuid
     * @return menu
     */
    public InventoryGUI getMenuByPlayerUUID(UUID uuid) {
        return this.menus.get(uuid);
    }
}
