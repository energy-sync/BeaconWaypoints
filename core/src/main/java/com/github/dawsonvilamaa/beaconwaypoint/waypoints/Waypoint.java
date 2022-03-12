package com.github.dawsonvilamaa.beaconwaypoint.waypoints;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Waypoint implements Cloneable {
    private String name;
    private WaypointCoord coord;
    private Material icon;
    private UUID ownerUUID;
    private boolean isWaypoint;

    /**
     * @param ownerUUID
     */
    public Waypoint(UUID ownerUUID, WaypointCoord coord) {
        this.name = null;
        this.coord = coord;
        this.icon = Material.BEACON;
        this.ownerUUID = ownerUUID;
        this.isWaypoint = false;
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
     * @param startWaypoint
     * @param destinationWaypoint
     * @param player If group teleporting is enabled, set to null, otherwise set to the player who is teleporting
     */
    public static void teleport(Waypoint startWaypoint, Waypoint destinationWaypoint, Player player) {
        FileConfiguration config = Main.plugin.getConfig();
        boolean instantTeleport = config.getBoolean("instant-teleport");
        boolean disableAnimations = config.getBoolean("disable-animations");

        int startBeaconStatus = startWaypoint.getBeaconStatus();
        int destinationBeaconStatus = destinationWaypoint.getBeaconStatus();

        WaypointCoord startCoord = startWaypoint.getCoord();
        Location startLoc = startCoord.getLocation();
        startLoc.setX(startLoc.getX() + 0.5);
        startLoc.setY(startLoc.getY() + 1);
        startLoc.setZ(startLoc.getZ() + 0.5);

        WaypointCoord destinationCoord = destinationWaypoint.getCoord();
        Location tpLoc = new Location(Bukkit.getWorld(destinationCoord.getWorldName()), destinationCoord.getX(), destinationCoord.getY(), destinationCoord.getZ());
        tpLoc.setX(tpLoc.getX() + 0.5);
        tpLoc.setY(Objects.requireNonNull(tpLoc.getWorld()).getMaxHeight() + 256);
        tpLoc.setZ(tpLoc.getZ() + 0.5);

        if (!instantTeleport && !disableAnimations) {
            //spawn warm-up particles and play warm-up sound
            Objects.requireNonNull(startLoc.getWorld()).spawnParticle(Particle.PORTAL, startLoc, 500, 1, 1, 1);
            startLoc.getWorld().playSound(startLoc, Sound.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 1, 1);
            startLoc.getWorld().playSound(startLoc, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1, 1);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!disableAnimations) {
                    //spawn launch particles and play launch sound
                    Objects.requireNonNull(startLoc.getWorld()).spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, startLoc, 50);
                    startLoc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, startLoc, 500, 1, 1, 1);
                    startLoc.getWorld().playSound(startLoc, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.BLOCKS, 2, 1);
                    startLoc.getWorld().playSound(startLoc, Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1, 0.65F);
                }

                //get all entities on the beacon
                Objects.requireNonNull(Bukkit.getWorld(startWaypoint.getWorldName())).getNearbyEntities(startLoc, 0.5, 0.5, 0.5).forEach(entity -> {
                    if (entity.getType() == EntityType.PLAYER && (player == null || entity.getUniqueId().equals(player.getUniqueId()))) {
                        WaypointPlayer waypointPlayer = Main.waypointManager.getPlayer(entity.getUniqueId());
                        if (waypointPlayer == null) {
                            Main.waypointManager.addPlayer(entity.getUniqueId());
                            waypointPlayer = Main.waypointManager.getPlayer(entity.getUniqueId());
                        }
                        waypointPlayer.setTeleporting(true);

                        ((Player) entity).closeInventory();
                        int startBeamTop = startBeaconStatus == 1 ? entity.getWorld().getMaxHeight() + 256 : startBeaconStatus - 2;
                        int destinationBeamTop = destinationBeaconStatus == 1 ? entity.getWorld().getMaxHeight() + 256 : destinationBeaconStatus - 2;
                        tpLoc.setY(destinationBeamTop);

                        //keep players in start beam
                        WaypointPlayer finalWaypointPlayer = waypointPlayer;
                        new BukkitRunnable() {
                            int time = 0;
                            @Override
                            public void run() {
                                //give entities resistance and levitation
                                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 255, false, false));
                                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 600, 127, false, false));

                                if (entity.getLocation().getY() < startBeamTop) {
                                    Location startBeamLoc = new Location(entity.getWorld(), startLoc.getX(), entity.getLocation().getY(), startLoc.getZ());
                                    if (entity.getLocation().distance(startBeamLoc) > 0.125) {
                                        startBeamLoc.setDirection(entity.getLocation().getDirection());
                                        entity.teleport(startBeamLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                                        entity.setVelocity(new Vector(0, 5, 0));
                                    }
                                    time++;

                                    //let player go if they are stuck after 30 seconds
                                    if (time >= 80) {
                                        ((LivingEntity) entity).removePotionEffect(PotionEffectType.LEVITATION);
                                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 255, false, false));
                                        Objects.requireNonNull(finalWaypointPlayer).setTeleporting(false);
                                        this.cancel();
                                    }
                                }
                                else {
                                    this.cancel();

                                    //teleport player to new beam
                                    tpLoc.setDirection(entity.getLocation().getDirection());
                                    entity.teleport(tpLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                                    entity.setVelocity(new Vector(0, -2, 0));
                                    ((LivingEntity) entity).removePotionEffect(PotionEffectType.LEVITATION);

                                    //keep players in destination beam
                                    new BukkitRunnable() {
                                        int time = 0;
                                        @Override
                                        public void run() {
                                            if (entity.getLocation().getY() > destinationWaypoint.getCoord().getY() + 1) {
                                                Location destinationBeamLoc = new Location(destinationCoord.getLocation().getWorld(), tpLoc.getX(), entity.getLocation().getY(), tpLoc.getZ());
                                                if (entity.getLocation().distance(destinationBeamLoc) > 0.125) {
                                                    destinationBeamLoc.setDirection(entity.getLocation().getDirection());
                                                    entity.teleport(destinationBeamLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                                                    entity.setVelocity(new Vector(0, -2, 0));
                                                }
                                                time++;

                                                //let player go if they are stuck after 30 seconds
                                                if (time >= 80) {
                                                    ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 255, false, false));
                                                    Objects.requireNonNull(finalWaypointPlayer).setTeleporting(false);
                                                    this.cancel();
                                                }
                                            }
                                            else {
                                                Objects.requireNonNull(finalWaypointPlayer).setTeleporting(false);
                                                this.cancel();
                                            }
                                        }
                                    }.runTaskTimer(Main.plugin, 0, 5);
                                }
                            }
                        }.runTaskTimer(Main.plugin, 0, 5);
                    }
                });
            }
        }.runTaskLater(Main.plugin, instantTeleport ? 0 : 50);
    }

    /**
     * @return 0 if beacon cannot be teleported to, 1 if it is able to be teleported to, returns y-coordinate of bedrock if there is bedrock above the beacon
     */
    public int getBeaconStatus() {
        final List<Material> pyramidBlocks = Main.version.getPyramidBlocks();
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
        return jsonWaypoint;
    }

    public Waypoint clone() {
        Waypoint clonedWaypoint = new Waypoint(this.ownerUUID, this.coord);
        clonedWaypoint.setName(this.name);
        clonedWaypoint.setIcon(this.icon);
        clonedWaypoint.setIsWaypoint(this.isWaypoint);
        return clonedWaypoint;
    }
}