package com.github.dawsonvilamaa.beaconwaypoint.gui;

import com.github.dawsonvilamaa.beaconwaypoint.LanguageManager;
import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.version.VersionWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class MultiPageInventoryGUI {
    String title;
    Player player;
    private ArrayList<InventoryGUIButton> buttons;
    InventoryGUI gui;
    InventoryGUI previousGUI;
    int numRows;
    int bottomRowSlot;

    /**
     * Creates an inventory GUI with a given name and number of rows. Rows must be a value from 1-5
     * If previousGUI is not null, a back button will be added to the bottom left corner of the menu
     *
     * @param player
     * @param title
     * @param rows
     * @param previousGUI
     */
    public MultiPageInventoryGUI(Player player, String title, int rows, InventoryGUI previousGUI) {
        this.player = player;
        this.title = title;
        this.buttons = new ArrayList<>();
        this.gui = new InventoryGUI(this.player, this.title, rows + 1, true);
        this.previousGUI = previousGUI;
        this.numRows = rows;
        this.bottomRowSlot = rows * 9;
    }

    /**
     * Adds an item to the inventory with a given name, description, and material
     * @param button
     * @return button
     */
    public InventoryGUIButton addButton(InventoryGUIButton button) {
        this.buttons.add(button);
        //this.gui.setSlot(this.gui.getSlot() + 1);
        return button;
    }

    /**
     * @return buttons
     */
    public ArrayList<InventoryGUIButton> getButtons() {
        return this.buttons;
    }

    /**
     * Shows the MultiPageInventoryGUI at a specific page
     * @param page
     */
    public void showPage(int page) {
        LanguageManager languageManager = Main.getLanguageManager();

        this.gui = new InventoryGUI(this.player, this.title, this.numRows + 1, true);

        //back button if previous GUI was provided
        if (this.previousGUI != null) {
            //InventoryGUIButton backButton = GUIs.headManager.createHeadButton(this.gui, ChatColor.WHITE + languageManager.getString("back"), ChatColor.DARK_GRAY + this.previousGUI.getTitle(), "MHF_ArrowLeft");
            InventoryGUIButton backButton = new InventoryGUIButton(this.gui, ChatColor.WHITE + languageManager.getString("back"), ChatColor.DARK_GRAY + this.previousGUI.getTitle(), Material.PLAYER_HEAD);
            backButton.setPlayerHead("MHF_ArrowLeft"); // a68f0b64-8d14-4000-a95f-4b9ba14f8df9
            backButton.setOnClick(e -> {
                this.previousGUI.showMenu();
            });
            gui.setButton(this.bottomRowSlot, backButton);
            gui.setButton(0, null);
        }

        //previous page button
        if (page > 0) {
            //InventoryGUIButton previousButton = GUIs.headManager.createHeadButton(gui, languageManager.getString("previous-page"), null, "MHF_ArrowLeft");
            InventoryGUIButton previousButton = new InventoryGUIButton(gui, languageManager.getString("previous-page"), null, Material.PLAYER_HEAD);
            previousButton.setPlayerHead("MHF_ArrowLeft"); // a68f0b64-8d14-4000-a95f-4b9ba14f8df9
            previousButton.setOnClick(e -> {
                showPage(page - 1);
            });
            gui.setButton(this.bottomRowSlot + 3, previousButton);
        }
        else gui.removeButton(this.bottomRowSlot + 3);

        //compass icon
        if (this.buttons.size() > this.numRows * 9)
            gui.setButton(this.bottomRowSlot + 4, new InventoryGUIButton(gui, null, null, Material.COMPASS));

        //next page button
        if (this.buttons.size() > (page + 1) * this.numRows * 9) {
            //InventoryGUIButton nextButton = GUIs.headManager.createHeadButton(gui, languageManager.getString("next-page"), null, "MHF_ArrowRight");
            InventoryGUIButton nextButton = new InventoryGUIButton(gui, languageManager.getString("next-page"), null, Material.PLAYER_HEAD);
            nextButton.setPlayerHead("MHF_ArrowRight"); // 50c8510b-5ea0-4d60-be9a-7d542d6cd156
            nextButton.setOnClick(e -> {
                showPage(page + 1);
            });
            gui.setButton(this.bottomRowSlot + 5, nextButton);
        }
        else gui.removeButton(this.bottomRowSlot + 5);

        //waypoint buttons
        int slot = 0;
        for (int buttonIndex = page * this.numRows * 9; buttonIndex < (page + 1) * this.numRows * 9; buttonIndex++) {
            if (buttonIndex < this.buttons.size() && this.buttons.get(buttonIndex) != null)
                gui.setButton(slot, this.buttons.get(buttonIndex));
            else gui.removeButton(slot);
            slot++;
        }

        gui.showMenu();
    }

    /**
     * Shows the MultiPageInventoryGUI from the first page
     */
    public void showMenu() {
        this.showPage(0);
    }

    /**
     * @return Starting slot of the bottom row of each page
     */
    public int getBottomRowSlot() {
        return this.bottomRowSlot;
    }

    /**
     * @return gui
     */
    public InventoryGUI getGUI() {
        return this.gui;
    }
}