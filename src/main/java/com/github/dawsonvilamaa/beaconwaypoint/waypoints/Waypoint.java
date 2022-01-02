package com.github.dawsonvilamaa.beaconwaypoint.waypoints;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.List;

public class Waypoint {
    private String name;
    private WaypointCoord coord;
    private Material icon;

    /**
     * @param name
     * @param location
     */
    public Waypoint(String name, Location location) {
        this.name = name;
        this.coord = new WaypointCoord(location);
        this.icon = Material.BEACON;
    }

    public Waypoint(JSONObject jsonWaypoint) {
        this.name = jsonWaypoint.get("name").toString();
        int x = Integer.parseInt(jsonWaypoint.get("x").toString());
        int y = Integer.parseInt(jsonWaypoint.get("y").toString());
        int z = Integer.parseInt(jsonWaypoint.get("z").toString());
        String worldName = jsonWaypoint.get("world").toString();
        this.coord = new WaypointCoord(x, y, z, worldName);
        this.icon = Material.valueOf(jsonWaypoint.get("icon").toString());
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return x
     */
    public int getX() {
        return this.coord.getX();
    }

    /**
     * @return y
     */
    public int getY() {
        return this.coord.getY();
    }

    /**
     * @return z
     */
    public int getZ() {
        return this.coord.getZ();
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
     * @param startWaypoint
     * @param destinationWaypoint
     */
    public static void teleport(Waypoint startWaypoint, Waypoint destinationWaypoint) {
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
        tpLoc.setY(tpLoc.getWorld().getMaxHeight() + 256);
        tpLoc.setZ(tpLoc.getZ() + 0.5);


        //spawn warm-up particles and play warm-up sound
        startLoc.getWorld().spawnParticle(Particle.PORTAL, startLoc, 500, 1, 1, 1);
        startLoc.getWorld().playSound(startLoc, Sound.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 1, 1);
        startLoc.getWorld().playSound(startLoc, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                //spawn launch particles and play launch sound
                startLoc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, startLoc, 50);
                startLoc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, startLoc, 500, 1, 1, 1);
                startLoc.getWorld().playSound(startLoc, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.BLOCKS, 2, 1);
                startLoc.getWorld().playSound(startLoc, Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1, 0.65F);

                //get all entities on the beacon
                Bukkit.getWorld(startWaypoint.getWorldName()).getNearbyEntities(startLoc, 0.5, 0.5, 0.5).forEach(entity -> {
                    if (entity.getType() == EntityType.PLAYER) {
                        int startBeamTop = startBeaconStatus == 1 ? entity.getWorld().getMaxHeight() + 256 : startBeaconStatus - 2;
                        int destinationBeamTop = destinationBeaconStatus == 1 ? entity.getWorld().getMaxHeight() + 256 : destinationBeaconStatus - 2;
                        tpLoc.setY(destinationBeamTop);

                        //keep players in start beam
                        new BukkitRunnable() {
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
                                        @Override
                                        public void run() {
                                            if (entity.getLocation().getY() > destinationWaypoint.getCoord().getY() + 1) {
                                                Location destinationBeamLoc = new Location(destinationCoord.getLocation().getWorld(), tpLoc.getX(), entity.getLocation().getY(), tpLoc.getZ());
                                                if (entity.getLocation().distance(destinationBeamLoc) > 0.125) {
                                                    destinationBeamLoc.setDirection(entity.getLocation().getDirection());
                                                    entity.teleport(destinationBeamLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                                                    entity.setVelocity(new Vector(0, -2, 0));
                                                }
                                            }
                                            else this.cancel();
                                        }
                                    }.runTaskTimer(Main.plugin, 0, 5);
                                }
                            }
                        }.runTaskTimer(Main.plugin, 0, 5);
                    }
                });
            }
        }.runTaskLater(Main.plugin, 50);
    }

    /**
     * @return 0 if no, 1 if yes, returns y-coordinate of bedrock if there is bedrock above the beacon
     */
    public int getBeaconStatus() {
        final List<Material> pyramidBlocks = Arrays.asList(Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK, Material.NETHERITE_BLOCK);
        int isActive = 1;

        Location beaconLoc = this.coord.getLocation();

        //check if there are pyramid blocks under the beacon
        for (int blockX = beaconLoc.getBlockX() - 1; blockX <= beaconLoc.getBlockX() + 1; blockX++) {
            for (int blockZ = beaconLoc.getBlockZ() - 1; blockZ <= beaconLoc.getBlockZ() + 1; blockZ++) {
                if (!pyramidBlocks.contains(beaconLoc.getWorld().getBlockAt(blockX, beaconLoc.getBlockY() - 1, blockZ).getType())) {
                    isActive = 0;
                    break;
                }
            }
        }

        if (isActive != 0) {
            //check if there are opaque blocks above the beacon
            for (int blockY = beaconLoc.getBlockY() + 1; blockY < beaconLoc.getWorld().getMaxHeight(); blockY++) {
                Block block = beaconLoc.getWorld().getBlockAt(beaconLoc.getBlockX(), blockY, beaconLoc.getBlockZ());
                if (block.getType() != Material.AIR && block.getType() != Material.VOID_AIR) {
                    if (block.getType() == Material.BEDROCK)
                        isActive = blockY;
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
        return jsonWaypoint;
    }
}