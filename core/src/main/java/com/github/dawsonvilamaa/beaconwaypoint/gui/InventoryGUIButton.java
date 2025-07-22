package com.github.dawsonvilamaa.beaconwaypoint.gui;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Consumer;

import java.util.Arrays;
import java.util.Objects;

public class InventoryGUIButton {
    private String name;
    private String description;
    private Material material;
    private int slot;
    private ItemStack item;
    private boolean locked;
    private InventoryGUI parentGUI;
    private Consumer<InventoryClickEvent> onClick;
    private Consumer<InventoryClickEvent> onRightClick;

    /**
     * @param parentGUI
     * @param name
     * @param description
     * @param material
     */
    public InventoryGUIButton(InventoryGUI parentGUI, String name, String description, Material material) {
        ItemStack newItem = new ItemStack(material, 1);
        ItemMeta meta = newItem.getItemMeta();
        if (name != null) {
            this.name = name;
            Objects.requireNonNull(meta).setDisplayName(name);
        }
        if (description != null) {
            this.description = description;
            String[] lines = description.split("\n");
            Objects.requireNonNull(meta).setLore(Arrays.asList(lines));
        }
        this.material = material;
        if (material != Material.AIR) {
            Objects.requireNonNull(meta).addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            newItem.setItemMeta(meta);
        }
        this.item = newItem;
        this.locked = true;
        this.parentGUI = parentGUI;
        this.slot = parentGUI.getSlot();
        this.onClick = null;
        this.onRightClick = null;
    }

    /**
     * Secondary constructor that lets you specify whether the button can be moved by the player
     * @param parentGUI
     * @param name
     * @param description
     * @param material
     * @param locked
     */
    public InventoryGUIButton(InventoryGUI parentGUI, String name, String description, Material material, boolean locked) {
        this(parentGUI, name, description, material);
        this.locked = locked;
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
        ItemMeta meta = this.item.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName(name);
        this.item.setItemMeta(meta);
        this.parentGUI.getInventory().setItem(this.slot, this.item);
    }

    /**
     * @return description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
        if (description != null) {
            ItemMeta meta = this.item.getItemMeta();
            String[] lines = description.split("\n");
            Objects.requireNonNull(meta).setLore(Arrays.asList(lines));
            this.item.setItemMeta(meta);
        }
        this.parentGUI.getInventory().setItem(this.slot, this.item);
    }

    /**
     * @return material
     */
    public Material getMaterial() {
        return this.material;
    }

    /**
     * @return item
     */
    public ItemStack getItem() {
        return this.item;
    }

    /**
     * @param item
     */
    public void setItem(ItemStack item) {
        this.item = item.clone();
        this.parentGUI.getInventory().setItem(this.slot, item);
    }

    /**
     * @return locked
     */
    public boolean isLocked() {
        return this.locked;
    }

    /**
     * @param locked
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * @return
     */
    public InventoryGUI getParentGUI() {
        return this.parentGUI;
    }

    /**
     * @param e
     */
    public void onClick(InventoryClickEvent e) {
        if (this.onClick != null) {
            try {
                this.onClick.accept(e);
            }
            catch (Throwable ex) {
                e.setCancelled(true);
                StringBuilder stackTraceStr = new StringBuilder(ex.getMessage()).append("\n");
                for (StackTraceElement frame : ex.getStackTrace()) {
                    stackTraceStr.append(frame.toString()).append("\n");
                }
                Main.getPlugin().getLogger().severe(stackTraceStr.toString());
            }
        }
    }

    /**
     * @param consumer
     */
    public void setOnClick(Consumer<InventoryClickEvent> consumer) {
        this.onClick = consumer;
    }

    /**
     * @return onClick
     */
    public Consumer<InventoryClickEvent> getOnClick() {
        return this.onClick;
    }

    /**
     * @param e
     */
    public void onRightClick(InventoryClickEvent e) {
        if (this.onRightClick != null)
            this.onRightClick.accept(e);
    }

    /**
     * @param consumer
     */
    public void setOnRightClick(Consumer<InventoryClickEvent> consumer) {
        this.onRightClick = consumer;
    }

    /**
     * @return onRightClick
     */
    public Consumer<InventoryClickEvent> getOnRightClick() {
        return this.onRightClick;
    }

    /**
     * @param compare
     * @return is equal
     */
    public boolean equals(InventoryGUIButton compare) {
        return this.name.equals(compare.getName()) && this.description.equals(compare.getDescription()) && this.material == compare.material;
    }
}