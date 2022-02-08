package com.github.dawsonvilamaa.beaconwaypoint.gui;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class InventoryGUI implements Listener, InventoryHolder {
    private Player player;
    private String title;
    private Inventory inventory;
    private HashMap<Integer, InventoryGUIButton> buttons;
    private int slot;
    private int maxItems;
    private boolean locked;
    private BukkitRunnable runnable;

    /**
     * Creates an inventory GUI with a given name and number of rows. Rows must be a value from 1-6
     * @param player
     * @param title
     * @param rows
     */
    public InventoryGUI(Player player, String title, int rows, boolean locked) {
        this.player = player;
        this.title = title;
        if (rows < 1) rows = 1;
        if (rows > 6) rows = 6;
        this.inventory = Bukkit.createInventory(null, 9 * rows, title);
        this.buttons = new HashMap<>();
        this.slot = 0;
        this.maxItems = (9 * rows) - 1;
        this.locked = locked;
        this.runnable = null;
    }

    /**
     * @return title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @return player
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return name
     */
    public String getName() {
        return this.title;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * @param inventory
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * @return locked
     */
    public boolean isLocked() {
        return this.locked;
    }

    /**
     * @return buttons
     */
    public HashMap<Integer, InventoryGUIButton> getButtons() {
        return this.buttons;
    }

    /**
     * Adds an item to the inventory with a given name, description, and material
     * @param button
     * @return button
     */
    public InventoryGUIButton addButton(InventoryGUIButton button) {
        //create item
        if (this.slot <= this.maxItems) {
            this.inventory.setItem(slot, button.getItem());
            this.buttons.put(slot, button);
            this.slot++;
        }
        return button;
    }

    /**
     * Adds an item to the inventory with a given name, description, and material. Multiple of the same item can be added using this method.
     * @param button
     * @param amount
     */
    public void addButtons(InventoryGUIButton button, int amount) {
        for (int i = 0; i < amount; i++)
            addButton(button);
    }

    /**
     * Sets a button at a specific slot in the inventory
     * @param slot
     * @param button
     * @return
     */
    public InventoryGUIButton setButton(int slot, InventoryGUIButton button) {
        this.inventory.setItem(slot, button.getItem());
        this.buttons.put(slot, button);
        return button;
    }

    /**
     * Removes the button from the given slot
     * @param slot
     */
    public void removeButton(int slot) {
        this.buttons.remove(slot);
    }

    /**
     * @return slot
     */
    public int getSlot() {
        return this.slot;
    }

    /**
     * @param slot
     */
    public void setSlot(int slot) {
        this.slot = slot;
    }

    /**
     * @return runnable
     */
    public BukkitRunnable getRunnable() {
        return this.runnable;
    }

    /**
     * @param runnable
     */
    public void setRunnable(BukkitRunnable runnable) {
        this.runnable = runnable;
        this.runnable.runTaskTimer(Main.plugin, 2, 2);
    }

    /**
     * Stops the GUI's runnable
     */
    public void stopRunnable() {
        this.runnable.cancel();
    }

    /**
     * Opens this GUI for the player
     */
    public void showMenu() {
        Main.menuManager.addMenu(player.getUniqueId(), this);
        this.player.openInventory(this.inventory);
    }

    /**
     * Removes all click events from the buttons in this GUI
     */
    public void removeAllClickEvents() {
        for (int i = 0; i < this.slot; i++)
            this.buttons.get(i).setOnClick(null);
    }
}