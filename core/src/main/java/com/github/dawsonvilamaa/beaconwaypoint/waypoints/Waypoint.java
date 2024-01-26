package com.github.dawsonvilamaa.beaconwaypoint.waypoints;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.github.dawsonvilamaa.beaconwaypoint.LanguageManager;
import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.MathHelper;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.*;

public class Waypoint implements Cloneable {
    private String name;
    private WaypointCoord coord;
    private Material icon;
    private UUID ownerUUID;
    private boolean isWaypoint;
    private boolean pinned;
    private ArrayList<UUID> playersDiscovered;
    private ArrayList<UUID> sharedPlayers;

    /**
     * @param ownerUUID
     */
    public Waypoint(UUID ownerUUID, WaypointCoord coord) {
        this.name = null;
        this.coord = coord;
        this.icon = Material.BEACON;
        this.ownerUUID = ownerUUID;
        this.isWaypoint = false;
        this.pinned = false;
        this.playersDiscovered = new ArrayList<>();
        this.sharedPlayers = new ArrayList<>();
    }

    /**
     * @param jsonWaypoint
     */
    public Waypoint(JSONObject jsonWaypoint) {
        Object jsonName = jsonWaypoint.get("name");
        this.name = jsonName == null ? null : jsonName.toString();
        int x = Integer.parseInt(jsonWaypoint.get("x").toString());
        int y = Integer.parseInt(jsonWaypoint.get("y").toString());
        int z = Integer.parseInt(jsonWaypoint.get("z").toString());
        String worldName = jsonWaypoint.get("world").toString();
        this.coord = new WaypointCoord(x, y, z, worldName);
        this.icon = Material.valueOf(jsonWaypoint.get("icon").toString());
        this.ownerUUID = UUID.fromString(jsonWaypoint.get("ownerUUID").toString());
        this.isWaypoint = Boolean.parseBoolean(jsonWaypoint.get("isWaypoint").toString());
        Object jsonPinned = (Object) jsonWaypoint.get("pinned");
        this.pinned = jsonPinned == null ? false : Boolean.parseBoolean(jsonWaypoint.get("pinned").toString());
        JSONArray jsonPlayersDiscovered = (JSONArray) jsonWaypoint.get("playersDiscovered");
        this.playersDiscovered = new ArrayList<>();
        if (jsonPlayersDiscovered != null) {
            for (Object jsonPlayer : jsonPlayersDiscovered)
                this.playersDiscovered.add(UUID.fromString((String) jsonPlayer));
        }
        JSONArray jsonSharedPlayers = (JSONArray) jsonWaypoint.get("sharedPlayers");
        this.sharedPlayers = new ArrayList<>();
        if (jsonSharedPlayers != null) {
            for (Object jsonPlayer : jsonSharedPlayers)
                this.sharedPlayers.add(UUID.fromString((String) jsonPlayer));
        }
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return lowerCaseName
     */
    public String getLowerCaseName() {
        return this.name.toLowerCase();
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return worldName
     */
    public String getWorldName() {
        return this.coord.getWorldName();
    }

    /**
     * @return coord
     */
    public WaypointCoord getCoord() {
        return this.coord;
    }

    /**
     * @param coord
     */
    public void setCoord(WaypointCoord coord) {
        this.coord = coord;
    }

    /**
     * @return material
     */
    public Material getIcon() {
        return this.icon;
    }

    /**
     * @param material
     */
    public void setIcon(Material material) {
        Material newMaterial = Material.getMaterial(material.toString());
        if (newMaterial != null)
            this.icon = material;
    }

    /**
     * @return ownerUUID
     */
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    /**
     * @param ownerUUID
     */
    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    /**
     * @return isWaypoint
     */
    public boolean isWaypoint() {
        return this.isWaypoint;
    }

    /**
     * @param isWaypoint
     */
    public void setIsWaypoint(boolean isWaypoint) {
        this.isWaypoint = isWaypoint;
    }

    /**
     * Returns whether this waypoint is pinned in the public list
     * @return pinned
     */
    public boolean isPinned() {
        return pinned;
    }

    /**
     * Sets whether this waypoint is pinned in the public list
     * @param pinned
     */
    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    /**
     * @return 0 if beacon cannot be teleported to, 1 if it is able to be teleported to, returns y-coordinate of bedrock if there is bedrock above the beacon
     */
    public int getBeaconStatus() {
        final List<Material> pyramidBlocks = Main.getVersionWrapper().getPyramidBlocks();
        int isActive = 1;

        Location beaconLoc = this.coord.getLocation();

        //check if beacon is there
        if (beaconLoc.getWorld().getBlockAt(beaconLoc).getType() != Material.BEACON)
            isActive = 0;

        if (isActive != 0) {
            //check if there are pyramid blocks under the beacon
            for (int blockX = beaconLoc.getBlockX() - 1; blockX <= beaconLoc.getBlockX() + 1; blockX++) {
                for (int blockZ = beaconLoc.getBlockZ() - 1; blockZ <= beaconLoc.getBlockZ() + 1; blockZ++) {
                    if (!pyramidBlocks.contains(Objects.requireNonNull(beaconLoc.getWorld()).getBlockAt(blockX, beaconLoc.getBlockY() - 1, blockZ).getType())) {
                        isActive = 0;
                        break;
                    }
                }
            }
        }

        if (isActive != 0) {
            //check if there are opaque blocks above the beacon
            for (int blockY = beaconLoc.getBlockY() + 1; blockY < Objects.requireNonNull(beaconLoc.getWorld()).getMaxHeight(); blockY++) {
                Block block = beaconLoc.getWorld().getBlockAt(beaconLoc.getBlockX(), blockY, beaconLoc.getBlockZ());
                if (block.getType() != Material.AIR && block.getType() != Material.VOID_AIR) {
                    if (block.getType() == Material.BEDROCK)
                        return blockY;
                    else {
                        isActive = 0;
                        break;
                    }
                }
            }
        }

        return isActive;
    }

    /**
     * Adds a player to the list of players that have discovered this waypoint
     * @param player
     */
    public void addPlayerDiscovered(Player player) {
        this.playersDiscovered.add(player.getUniqueId());
    }

    /**
     * Checks if a player has discovered this waypoint
     * @param player
     * @return True if the player has discovered this waypoint, false if not
     */
    public boolean playerDiscoveredWaypoint(Player player) {
        return this.playersDiscovered.contains(player.getUniqueId());
    }

    /**
     * Returns a list of UUIDs for players who discovered this waypoint
     * @return playersDiscovered
     */
    public ArrayList<UUID> getPlayersDiscovered() {
        return playersDiscovered;
    }

    /**
     * Sets the list of UUIDs for players who discovered this waypoint
     * @param playersDiscovered
     */
    public void setPlayersDiscovered(ArrayList<UUID> playersDiscovered) {
        this.playersDiscovered = (ArrayList<UUID>) playersDiscovered.clone();
    }

    /**
     * Gives a player access to this waypoint if private
     * @param uuid
     */
    public void givePlayerAccess(UUID uuid, String username) {
        WaypointManager waypointManager = Main.getWaypointManager();
        if (waypointManager.getPlayer(uuid) == null)
            waypointManager.addPlayer(uuid, username);
        if (!this.sharedPlayers.contains(uuid))
            this.sharedPlayers.add(uuid);
    }

    /**
     * Revokes a player's access to this waypoint if private
     * @param uuid
     */
    public void removePlayerAccess(UUID uuid) {
        this.sharedPlayers.remove(uuid);
    }

    /**
     * Checks if a player has been given access to this waypoint if private
     * @param uuid
     * @return Whether the player has access to this waypoint
     */
    public boolean sharedWithPlayer(UUID uuid) {
        return this.sharedPlayers.contains(uuid);
    }

    /**
     * Returns a list of UUIDs for players who have access to this waypoint if private
     * @return
     */
    public ArrayList<UUID> getSharedPlayers() {
        return this.sharedPlayers;
    }

    /**
     * Sets the list of UUIDs for players who have been given access to this waypoint if private
     * @param sharedPlayers
     */
    public void setSharedPlayers(ArrayList<UUID> sharedPlayers) {
        this.sharedPlayers = (ArrayList<UUID>) sharedPlayers.clone();
    }

    /**
     * @return jsonWaypoint
     */
    public JSONObject toJSON() {
        JSONObject jsonWaypoint = new JSONObject();
        jsonWaypoint.put("name", this.name);
        jsonWaypoint.put("x", String.valueOf(this.coord.getX()));
        jsonWaypoint.put("y", String.valueOf(this.coord.getY()));
        jsonWaypoint.put("z", String.valueOf(this.coord.getZ()));
        jsonWaypoint.put("world", this.coord.getWorldName());
        jsonWaypoint.put("icon", this.icon.toString());
        jsonWaypoint.put("ownerUUID", this.ownerUUID.toString());
        jsonWaypoint.put("isWaypoint", String.valueOf(this.isWaypoint));
        jsonWaypoint.put("pinned", String.valueOf(this.pinned));
        JSONArray jsonPlayersDiscovered = new JSONArray();
        for (UUID uuid : this.playersDiscovered)
            jsonPlayersDiscovered.add(uuid.toString());
        jsonWaypoint.put("playersDiscovered", jsonPlayersDiscovered);
        JSONArray jsonSharedPlayers = new JSONArray();
        for (UUID uuid : this.sharedPlayers)
            jsonSharedPlayers.add(uuid.toString());
        jsonWaypoint.put("sharedPlayers", jsonSharedPlayers);
        return jsonWaypoint;
    }

    public Waypoint clone() {
        Waypoint clonedWaypoint = new Waypoint(this.ownerUUID, this.coord);
        clonedWaypoint.setName(this.name);
        clonedWaypoint.setIcon(this.icon);
        clonedWaypoint.setIsWaypoint(this.isWaypoint);
        clonedWaypoint.setPinned(this.pinned);
        clonedWaypoint.setPlayersDiscovered(this.playersDiscovered);
        clonedWaypoint.setSharedPlayers(this.sharedPlayers);
        return clonedWaypoint;
    }
}