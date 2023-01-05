package com.github.dawsonvilamaa.beaconwaypoint.waypoints;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.github.dawsonvilamaa.beaconwaypoint.LanguageManager;
import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.MathHelper;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.*;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class WaypointHelper {
    /**
     * @param startWaypoint
     * @param destinationWaypoint
     * @param player
     * @param groupTeleporting
     */
    public static void teleport(Waypoint startWaypoint, Waypoint destinationWaypoint, Player player, boolean groupTeleporting) {
        Main plugin = Main.getPlugin();
        FileConfiguration config = Main.getPlugin().getConfig();

        boolean instantTeleport = config.getBoolean("instant-teleport");
        boolean disableAnimations = config.getBoolean("disable-animations");
        if (!config.contains("launch-player"))
            config.set("launch-player", true);
        boolean launchPlayer = config.getBoolean("launch-player");

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
        tpLoc.setY(launchPlayer ? tpLoc.getWorld().getMaxHeight() + 256 : destinationCoord.getY() + 1);
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
                    if (entity.getType() == EntityType.PLAYER && (!groupTeleporting || entity.getUniqueId().equals(player.getUniqueId()))) {
                        WaypointManager waypointManager = Main.getWaypointManager();
                        WaypointPlayer waypointPlayer = waypointManager.getPlayer(entity.getUniqueId());
                        if (waypointPlayer == null) {
                            waypointManager.addPlayer(entity.getUniqueId());
                            waypointPlayer = waypointManager.getPlayer(entity.getUniqueId());
                        }

                        //if launch is disabled
                        if (!launchPlayer) {
                            tpLoc.setDirection(entity.getLocation().getDirection());
                            entity.teleport(tpLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        } else {
                            boolean ncpLoaded = Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus");
                            if (ncpLoaded)
                                NCPExemptionManager.exemptPermanently(entity.getUniqueId(), CheckType.MOVING_CREATIVEFLY);
                            waypointPlayer.setTeleporting(true);

                            ((Player) entity).closeInventory();

                            if (!config.contains("launch-player-height"))
                                config.set("launch-player-height", 576);
                            int launchPlayerHeight = config.getInt("launch-player-height");
                            int worldHeight = startLoc.getWorld().getMaxHeight();
                            if (launchPlayerHeight < worldHeight)
                                launchPlayerHeight = worldHeight;
                            int startBeamTop = startBeaconStatus == 1 ? launchPlayerHeight : startBeaconStatus - 2;
                            int destinationBeamTop = destinationBeaconStatus == 1 ? launchPlayerHeight : destinationBeaconStatus - 2;
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
                                    } else {
                                        this.cancel();

                                        //player pays fee
                                        if (pay(player, startWaypoint, destinationWaypoint)) {
                                            //teleport player to new beam
                                            tpLoc.setDirection(entity.getLocation().getDirection());
                                            if (ncpLoaded)
                                                NCPExemptionManager.unexempt(entity.getUniqueId(), CheckType.MOVING_CREATIVEFLY);
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
                                                    } else {
                                                        Objects.requireNonNull(finalWaypointPlayer).setTeleporting(false);
                                                        this.cancel();
                                                    }
                                                }
                                            }.runTaskTimer(plugin, 0, 5);
                                        }
                                        else {
                                            entity.setVelocity(new Vector(0, -2, 0));
                                            ((LivingEntity) entity).removePotionEffect(PotionEffectType.LEVITATION);
                                        }
                                    }
                                }
                            }.runTaskTimer(plugin, 0, 5);
                        }
                    }
                });
            }
        }.runTaskLater(plugin, instantTeleport ? 0 : 50);
    }

    /**
     * Deducts the XP or money from the player when they teleport
     * @param player
     * @param startWaypoint
     * @param destinationWaypoint
     * @return Whether the player still meets payment requirements right before teleporting
     */
    public static boolean pay(Player player, Waypoint startWaypoint, Waypoint destinationWaypoint) {
        FileConfiguration config = Main.getPlugin().getConfig();
        String paymentMode = config.getString("payment-mode");
        if (paymentMode == null)
            paymentMode = "none";

        int cost = calculateCost(startWaypoint, destinationWaypoint, paymentMode, config.getDouble(paymentMode + "-cost-per-chunk"), config.getDouble("cost-multiplier"));

        if (!checkPaymentRequirements(player, startWaypoint, destinationWaypoint, cost))
            return false;

        switch (paymentMode) {
            case "xp":
                MathHelper.setXp(player, -cost);
                break;

            case "money":
                IEssentials essentials = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
                if (essentials != null) {
                    User essentialsUser = essentials.getUser(player);
                    essentialsUser.takeMoney(BigDecimal.valueOf(cost));
                }
                break;
        }

        List<?> requiredItems = config.getList("required-items");
        double costMultiplier = config.getDouble("cost-multiplier");
        if (requiredItems != null && requiredItems.size() > 0) {
            for (Object o : requiredItems) {
                HashMap<String, Object> item = (HashMap<String, Object>) o;
                Object requiredMaterialObj = item.get("item");
                if (requiredMaterialObj == null)
                    continue;
                Material requiredMaterial = Material.getMaterial(requiredMaterialObj.toString());
                Object nameObj = item.get("name");
                String requiredName = nameObj != null ? nameObj.toString() : "";
                Object requiredAmountObj = item.get("amount");
                int requiredAmount = requiredAmountObj != null ? Integer.parseInt(requiredAmountObj.toString()) : 1;
                Object requiredDimensionAmountObj = item.get("dimension-amount");
                int requiredDimensionAmount = requiredDimensionAmountObj != null ? Integer.parseInt(requiredDimensionAmountObj.toString()) : 1;
                Object useMultiplierObj = item.get("use-multiplier");
                boolean useMultiplier = useMultiplierObj != null ? Boolean.parseBoolean(useMultiplierObj.toString()) : false;
                Object consumeItemObj = item.get("consume");
                boolean consumeItem = consumeItemObj != null ? Boolean.parseBoolean(consumeItemObj.toString()) : false;
                int itemCost;
                if (requiredDimensionAmountObj != null && !startWaypoint.getWorldName().equals(destinationWaypoint.getWorldName()))
                    itemCost = requiredDimensionAmount;
                else itemCost = calculateCost(startWaypoint, destinationWaypoint, requiredAmount, useMultiplier ? costMultiplier : 0);
                Bukkit.broadcastMessage("Item cost: " + itemCost);

                boolean matchName = requiredName != null;
                //check inventory for required items
                if (consumeItem) {
                    for (int invIndex = 0; invIndex < player.getInventory().getContents().length; invIndex++) {
                        ItemStack invItem = player.getInventory().getItem(invIndex);
                        if (invItem == null)
                            continue;
                        //check shulker boxes for items
                        if (invItem.getType() == Material.SHULKER_BOX) {
                            ItemStack newShulkerBox = invItem.clone();
                            BlockStateMeta itemMeta = (BlockStateMeta) newShulkerBox.getItemMeta();
                            ShulkerBox shulker = (ShulkerBox) itemMeta.getBlockState();
                            for (int shulkerIndex = 0; shulkerIndex < shulker.getInventory().getContents().length; shulkerIndex++) {
                                ItemStack shulkerItem = shulker.getInventory().getItem(shulkerIndex);
                                if (shulkerItem == null)
                                    continue;
                                int slotAmount = shulkerItem.getAmount();
                                if (itemCost > slotAmount) {
                                    itemCost -= slotAmount;
                                    shulker.getInventory().setItem(shulkerIndex, null);
                                }
                                else {
                                    int newSlotAmount = slotAmount - itemCost;
                                    itemCost = 0;
                                    shulkerItem.setAmount(newSlotAmount);
                                    shulker.getInventory().setItem(shulkerIndex, shulkerItem);
                                }
                                itemMeta.setBlockState(shulker);
                                newShulkerBox.setItemMeta(itemMeta);
                                player.getInventory().setItem(invIndex, newShulkerBox);
                            }
                        }
                        if (invItem.getType() == requiredMaterial && (!matchName || invItem.getItemMeta().getDisplayName().equals(requiredName))) {
                            int slotAmount = invItem.getAmount();
                            if (itemCost > slotAmount) {
                                itemCost -= slotAmount;
                                player.getInventory().setItem(invIndex, null);
                            }
                            else {
                                int newSlotAmount = slotAmount - itemCost;
                                itemCost = 0;
                                invItem.setAmount(newSlotAmount);
                            }
                        }
                    }
                }
            }

            Bukkit.broadcastMessage("cost <= 0: " + (cost <= 0) + "\ncost: " + cost);
            //return cost <= 0;
        }

        return true;
    }

    /**
     * Checks if the player needs to pay to teleport, checks if they have enough to pay, and returns whether the player can afford it
     * @param player
     * @param startWaypoint
     * @param destinationWaypoint
     * @return
     */
    public static boolean checkPaymentRequirements(Player player, Waypoint startWaypoint, Waypoint destinationWaypoint, int costPerChunk) {
        FileConfiguration config = Main.getPlugin().getConfig();
        LanguageManager languageManager = Main.getLanguageManager();

        boolean xpOrMoneyRequirementMet = true;
        double distance = MathHelper.distance2D(startWaypoint.getCoord(), destinationWaypoint.getCoord(), true);

        String paymentMode = config.getString("payment-mode");
        if (!(paymentMode.equals("xp") || paymentMode.equals("money") || paymentMode.equals("none"))) {
            Bukkit.getLogger().warning(languageManager.getString("payment-mode-not-found"));
            paymentMode = "none";
        }
        if (!paymentMode.equals("none")) {
            Bukkit.broadcastMessage("Cost per chunk: " + config.getDouble(paymentMode + "-cost-per-chunk") + "\nChunk distance: " + distance + "\nMultiplier: " + config.getDouble("cost-multiplier") + "\nCost: " + costPerChunk);

            switch (paymentMode) {
                case "xp":
                    int currentXp = MathHelper.getXpPoints(player);
                    int pointsNeeded = -(currentXp - costPerChunk);
                    if (pointsNeeded > 0) {
                        player.sendMessage(ChatColor.RED + languageManager.getString("insufficient-xp") + ": " + pointsNeeded);
                        xpOrMoneyRequirementMet = false;
                    }
                    break;

                case "money":
                    IEssentials essentials = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
                    if (essentials != null) {
                        User essentialsUser = essentials.getUser(player);
                        BigDecimal currentMoney = essentialsUser.getMoney();
                        BigDecimal moneyNeeded = currentMoney.subtract(BigDecimal.valueOf(costPerChunk)).multiply(BigDecimal.valueOf(-1));
                        if (moneyNeeded.compareTo(BigDecimal.ZERO) > 0) {
                            player.sendMessage(ChatColor.RED + languageManager.getString("insufficient-money") + ": $" + moneyNeeded);
                            xpOrMoneyRequirementMet = false;
                        }
                    }
                    break;
            }
        }

        return xpOrMoneyRequirementMet && checkRequiredItems(player, startWaypoint, destinationWaypoint);
    }

    /**
     * Checks if the player has the required items to teleport specified in the config
     * @param player
     * @return Whether player has the required items
     */
    public static boolean checkRequiredItems(Player player, Waypoint startWaypoint, Waypoint destinationWaypoint) {
        FileConfiguration config = Main.getPlugin().getConfig();
        List<?> requiredItems = config.getList("required-items");
        if (requiredItems == null || requiredItems.size() == 0)
            return true;

        boolean hasRequiredItems = true;
        double costMultiplier = config.getDouble("cost-multiplier");
        for (Object o : requiredItems) {
            HashMap<String, Object> item = (HashMap<String, Object>) o;
            Object requiredMaterialObj = item.get("item");
            if (requiredMaterialObj == null)
                continue;
            Material requiredMaterial = Material.valueOf(requiredMaterialObj.toString());
            Object nameObj = item.get("name");
            String requiredName = nameObj != null ? nameObj.toString() : "";
            Object useMultiplierObj = item.get("use-multiplier");
            boolean useMultiplier = useMultiplierObj != null ? Boolean.parseBoolean(useMultiplierObj.toString()) : false;
            Object requiredAmountObj = item.get("amount");
            int requiredAmount = requiredAmountObj != null ? Integer.parseInt(requiredAmountObj.toString()) : 1;
            Object requiredDimensionAmountObj = item.get("dimension-amount");
            int requiredDimensionAmount = requiredDimensionAmountObj != null ? Integer.parseInt(requiredDimensionAmountObj.toString()) : 1;
            int itemCost;
            if (requiredDimensionAmountObj != null && !startWaypoint.getWorldName().equals(destinationWaypoint.getWorldName()))
                itemCost = requiredDimensionAmount;
            else itemCost = calculateCost(startWaypoint, destinationWaypoint, requiredAmount, useMultiplier ? costMultiplier : 0);

            List<?> bannedItems = config.getList("banned-items");
            if (bannedItems == null)
                bannedItems = new ArrayList<String>();

            Bukkit.broadcastMessage(requiredMaterial + " required: " + itemCost);
            int count = 0;
            boolean matchName = requiredName != null;
            StringBuilder bannedItemsMsg = new StringBuilder(Main.getLanguageManager().getString("has-banned-items") + ": ");
            boolean hasBannedItems = false;

            //check inventory for required items
            for (ItemStack invItem : player.getInventory().getContents()) {
                if (invItem == null)
                    continue;

                //check for banned items
                boolean thisItemBanned = false;
                for (Object bannedItem : bannedItems) {
                    if (invItem.getType().toString().equals(bannedItem.toString())) {
                        hasBannedItems = true;
                        thisItemBanned = true;
                        if (!bannedItemsMsg.toString().contains(invItem.getType().toString()))
                            bannedItemsMsg.append(invItem.getType()).append(", ");
                    }
                }
                if (thisItemBanned)
                    continue;

                if (invItem.getType() == requiredMaterial && (!matchName || invItem.getItemMeta().getDisplayName().equals(requiredName))) {
                    count += invItem.getAmount();
                    Bukkit.broadcastMessage(invItem.getType() + " count: " + count);
                }

                //check shulker boxes for items
                else if (invItem.getType() == Material.SHULKER_BOX) {
                    BlockStateMeta itemMeta = (BlockStateMeta) invItem.getItemMeta();
                    ShulkerBox shulker = (ShulkerBox) itemMeta.getBlockState();
                    for (ItemStack shulkerItem : shulker.getInventory().getContents()) {
                        if (shulkerItem == null)
                            continue;

                        //check for banned items
                        thisItemBanned = false;
                        for (Object bannedItem : bannedItems) {
                            if (shulkerItem.getType().toString().equals(bannedItem.toString())) {
                                hasBannedItems = true;
                                thisItemBanned = true;
                                if (!bannedItemsMsg.toString().contains(shulkerItem.getType().toString()))
                                    bannedItemsMsg.append(shulkerItem.getType()).append(", ");
                            }
                        }
                        if (thisItemBanned)
                            continue;

                        if (shulkerItem.getType() == requiredMaterial && (!matchName || shulkerItem.getItemMeta().getDisplayName().equals(requiredName))) {
                            count += shulkerItem.getAmount();
                            Bukkit.broadcastMessage(shulkerItem.getType() + " count: " + count);
                        }
                    }
                }
            }

            if (hasBannedItems) {
                player.sendMessage(ChatColor.RED + bannedItemsMsg.substring(0, bannedItemsMsg.length() - 2));
                return false;
            }

            if (count < itemCost) {
                Bukkit.broadcastMessage(item.get("item") + " not found");
                hasRequiredItems = false;
            }
            else {
                Bukkit.broadcastMessage(item.get("item") + " found");
            }
        }

        if (!hasRequiredItems)
            player.sendMessage(ChatColor.RED + Main.getLanguageManager().getString("insufficient-items"));
        return hasRequiredItems;
    }

    /**
     * Calculates the cost of teleportation for XP and money costs based on config settings
     * @param startWaypoint
     * @param destinationWaypoint
     * @param paymentMode
     * @param costPerChunk
     * @param costMultiplier
     * @return cost
     */
    public static int calculateCost(Waypoint startWaypoint, Waypoint destinationWaypoint, String paymentMode, double costPerChunk, double costMultiplier) {
        if (paymentMode.equals("xp") || paymentMode.equals("money") || paymentMode.equals("none")) {
            if (!startWaypoint.getCoord().getWorldName().equals(destinationWaypoint.getCoord().getWorldName())) {
                FileConfiguration config = Main.getPlugin().getConfig();
                if (paymentMode.equals("xp"))
                    return config.getInt("xp-cost-dimension");
                else if (paymentMode.equals("money"))
                    return config.getInt("money-cost-dimension");
                else return 0;
            }

            if (costPerChunk < 0)
                costPerChunk = 0;
            if (costMultiplier < 0)
                costMultiplier = 0;
            int cost = (int) Math.round(costPerChunk * Math.pow(MathHelper.distance2D(startWaypoint.getCoord(), destinationWaypoint.getCoord(), true), costMultiplier));
            if (cost < 0)
                cost = 0;
            return cost;
        }
        else return 0;
    }

    /**
     * Calculates the cost of teleportation for required items based on config settings
     * @param startWaypoint
     * @param destinationWaypoint
     * @param costPerChunk
     * @param costMultiplier
     * @return cost
     */
    public static int calculateCost(Waypoint startWaypoint, Waypoint destinationWaypoint, int costPerChunk, double costMultiplier) {
        if (costPerChunk < 0)
            costPerChunk = 0;
        if (costMultiplier < 0)
            costMultiplier = 0;
        int cost = (int) Math.round(costPerChunk * Math.pow(MathHelper.distance2D(startWaypoint.getCoord(), destinationWaypoint.getCoord(), true), costMultiplier));
        if (cost < 0)
            cost = 0;
        return cost;
    }

    /**
     * Creates a description for a waypoint based on its properties
     * @param startWaypoint
     * @param destinationWaypoint
     * @return description
     */
    public static String getWaypointDescription(Waypoint startWaypoint, Waypoint destinationWaypoint) {
        StringBuilder description = new StringBuilder(ChatColor.RESET + "" + ChatColor.WHITE);
        FileConfiguration config = Main.getPlugin().getConfig();
        LanguageManager languageManager = Main.getLanguageManager();
        WaypointCoord startCoord = startWaypoint.getCoord();
        WaypointCoord destinationCoord = destinationWaypoint.getCoord();

        String paymentMode = config.getString("payment-mode");
        if (!(paymentMode.equals("xp") || paymentMode.equals("money") || paymentMode.equals("none"))) {
            Bukkit.getLogger().warning(languageManager.getString("payment-mode-not-found"));
            paymentMode = "none";
        }
        IEssentials essentials = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (paymentMode.equals("money") && essentials == null)
            paymentMode = "none";
        List<?> requiredItems = config.getList("required-items");
        if (requiredItems != null && requiredItems.size() > 0 || !paymentMode.equals("none"))
            description.append(ChatColor.YELLOW).append(languageManager.getString("cost")).append(": \n");

        //xp or money payment
        if (!paymentMode.equals("none")) {
            description.append(ChatColor.WHITE);
            if (paymentMode.equals("xp"))
                description.append(calculateCost(startWaypoint, destinationWaypoint, "xp", config.getInt("xp-cost-per-chunk"), config.getDouble("cost-multiplier"))).append(" XP\n");
            else if (paymentMode.equals("money") && essentials != null)
                description.append("$").append(calculateCost(startWaypoint, destinationWaypoint, "money", config.getInt("money-cost-per-chunk"), config.getDouble("cost-multiplier"))).append("\n");
        }

        //required items
        if (requiredItems != null && requiredItems.size() > 0) {
            ArrayList<String> consumedItemsList = new ArrayList<>();
            ArrayList<String> requiredItemsList = new ArrayList<>();
            double costMultiplier = config.getDouble("cost-multiplier");
            for (Object o : requiredItems) {
                HashMap<String, Object> item = (HashMap<String, Object>) o;
                Object requiredMaterialObj = item.get("item");
                if (requiredMaterialObj == null)
                    continue;
                Material requiredMaterial = Material.valueOf(requiredMaterialObj.toString());
                Object nameObj = item.get("name");
                String requiredName = nameObj != null ? nameObj.toString() : null;
                Object useMultiplierObj = item.get("use-multiplier");
                boolean useMultiplier = useMultiplierObj != null ? Boolean.parseBoolean(useMultiplierObj.toString()) : false;
                Object requiredAmountObj = item.get("amount");
                int requiredAmount = requiredAmountObj != null ? Integer.parseInt(requiredAmountObj.toString()) : 1;
                Object requiredDimensionAmountObj = item.get("dimension-amount");
                int requiredDimensionAmount = requiredDimensionAmountObj != null ? Integer.parseInt(requiredDimensionAmountObj.toString()) : 1;
                Object consumeItemObj = item.get("consume");
                boolean consumeItem = consumeItemObj != null ? Boolean.parseBoolean(consumeItemObj.toString()) : false;
                int itemCost;
                if (requiredDimensionAmountObj != null && !startCoord.getWorldName().equals(destinationCoord.getWorldName()))
                    itemCost = requiredDimensionAmount;
                else itemCost = calculateCost(startWaypoint, destinationWaypoint, requiredAmount, useMultiplier ? costMultiplier : 0);
                String itemStr = itemCost + "x ";
                if (requiredName == null)
                    itemStr += requiredMaterial;
                else itemStr += requiredName;
                if (consumeItem)
                    consumedItemsList.add(itemStr);
                else requiredItemsList.add(itemStr);
            }

            for (String item : consumedItemsList)
                description.append(ChatColor.WHITE).append(item).append("\n");

            if (requiredItemsList.size() > 0)
                description.append(ChatColor.YELLOW).append(languageManager.getString("required-items")).append(":\n");
            for (String item : requiredItemsList)
                description.append(ChatColor.WHITE).append(item).append("\n");
        }

        //distance
        if (startCoord.getWorldName().equals(destinationCoord.getWorldName()))
            description.append(ChatColor.GRAY).append(languageManager.getString("distance")).append(": ").append(Math.round(MathHelper.distance2D(startCoord, destinationCoord, false))).append(" ").append(languageManager.getString("blocks")).append("\n");
        description.append(ChatColor.GRAY).append(languageManager.getString("owner")).append(": ").append(Bukkit.getOfflinePlayer(destinationWaypoint.getOwnerUUID()).getName()).append("\n");
        description.append(ChatColor.DARK_GRAY).append(ChatColor.ITALIC).append(destinationWaypoint.getCoord().getWorldName()).append(" (").append(destinationCoord.getX()).append(", ").append(destinationCoord.getY()).append(", ").append(destinationCoord.getZ()).append(")");
        return description.toString();
    }

    public static final String[] DEFAULT_ALLOWED_WORLDS = new String[]{
            "world",
            "world_nether",
            "world_the_end"
    };

    public static final String[] DEFAULT_WAYPOINT_ICONS = new String[]{
            "APPLE",
            "SHROOMLIGHT",
            "TOTEM_OF_UNDYING",
            "EMERALD",
            "DIAMOND",
            "END_CRYSTAL",
            "LEATHER",
            "FILLED_MAP",
            "SNOW_BLOCK",
            "RED_MUSHROOM",
            "CARROT",
            "GOLDEN_APPLE",
            "CREEPER_HEAD",
            "PRISMARINE_BRICKS",
            "ALLIUM",
            "IRON_PICKAXE",
            "QUARTZ_BRICKS",
            "SKELETON_SKULL",
            "POPPY",
            "PUMPKIN",
            "HONEYCOMB",
            "SEA_LANTERN",
            "BLUE_ICE",
            "PURPUR_BLOCK",
            "ENCHANTING_TABLE",
            "OAK_LOG",
            "WHEAT",
            "RED_BED",
            "ORANGE_TULIP",
            "BLAZE_POWDER",
            "SUGAR_CANE",
            "LAPIS_LAZULI",
            "CHORUS_FRUIT",
            "END_PORTAL_FRAME",
            "ELYTRA",
            "BREWING_STAND",
            "REDSTONE",
            "RED_SAND",
            "END_STONE",
            "CACTUS",
            "WATER_BUCKET",
            "SHULKER_BOX",
            "CHEST",
            "NETHERITE_INGOT",
            "SOUL_SAND",
            "RED_NETHER_BRICKS",
            "MAGMA_BLOCK",
            "SAND",
            "ENDER_PEARL",
            "WARPED_STEM",
            "CRIMSON_STEM",
            "ZOMBIE_HEAD",
            "OBSIDIAN",
            "WITHER_SKELETON_SKULL",
            "GRASS_BLOCK",
            "IRON_BLOCK",
            "COPPER_BLOCK",
            "GOLD_BLOCK",
            "DIAMOND_BLOCK",
            "NETHERITE_BLOCK",
            "SPRUCE_LOG",
            "BIRCH_LOG",
            "JUNGLE_LOG",
            "ACACIA_LOG",
            "DARK_OAK_LOG",
            "SPONGE",
            "BOOKSHELF",
            "NETHERRACK",
            "GLOWSTONE",
            "STONE_BRICKS",
            "DEEPSLATE_BRICKS",
            "MELON",
            "MYCELIUM",
            "EMERALD_BLOCK",
            "HAY_BLOCK",
            "BAMBOO",
            "IRON_BARS",
            "DRAGON_HEAD",
            "CAMPFIRE",
            "BEE_NEST",
            "TNT",
            "BEACON",
            "QUARTZ",
            "IRON_INGOT",
            "COPPER_INGOT",
            "GOLD_INGOT",
            "STRING",
            "FEATHER",
            "GUNPOWDER",
            "WHEAT_SEEDS",
            "BRICK",
            "BOOK",
            "SLIME_BALL",
            "EGG",
            "BONE",
            "BLAZE_ROD",
            "GOLD_NUGGET",
            "NETHER_WART",
            "ENDER_EYE",
            "EXPERIENCE_BOTTLE",
            "NETHER_STAR",
            "FIREWORK_ROCKET",
            "MUSIC_DISC_STAL",
            "CAKE",
            "SWEET_BERRIES",
            "WHITE_WOOL",
            "GOLDEN_PICKAXE",
            "DIAMOND_PICKAXE",
            "NETHERITE_PICKAXE",
            "FISHING_ROD",
            "TRIDENT",
            "GOLDEN_CARROT"
    };
}