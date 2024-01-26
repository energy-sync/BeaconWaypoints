package com.github.dawsonvilamaa.beaconwaypoint;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigUpdater {
    /**
     * Checks for the plugin's config.yml file for missing fields and adds the default values
     * @param config
     * @throws IOException
     */
    public static void checkConfig(FileConfiguration config) throws IOException {
        Path configPath = Paths.get("plugins" + File.separator + "BeaconWaypoints" + File.separator + "config.yml");
        InputStreamReader is = new InputStreamReader(Files.newInputStream(configPath), StandardCharsets.UTF_8);
        String originalConfigStr = new BufferedReader(is).lines().collect(Collectors.joining("\n"));
        String configStr = originalConfigStr;

        if (!configStr.contains("max-public-waypoints:")) {
            configStr += "\n\n# The maximum amount of public waypoints that can exist at once on the server\n" +
                    "max-public-waypoints: 100";
        }
        if (!configStr.contains("max-private-waypoints:")) {
            configStr += "\n\n# The maximum amount of private waypoints that each player can have\n" +
                    "max-private-waypoints: 30";
        }
        if (!configStr.contains("force-alphanumeric-names:")) {
            configStr += "\n\n# Force waypoint names to be alphanumeric when they are created\n" +
                    "force-alphanumeric-names: false";
        }
        if (!configStr.contains("public-waypoint-menu-rows:")) {
            configStr += "\n\n# The number of rows the public waypoint selection menu will show per page, not including the row for page navigation\n" +
                    "# Range: 1-5\n" +
                    "public-waypoint-menu-rows: 3";
        }
        if (!configStr.contains("private-waypoint-menu-rows:")) {
            configStr += "\n\n# The number of rows the private waypoint selection menu will show per page, not including the row for page navigation\n" +
                    "# Range: 1-5\n" +
                    "private-waypoint-menu-rows: 2";
        }
        if (!configStr.contains("instant-teleport:")) {
            configStr += "\n\n# Activate teleportation as soon as the destination is chosen without a warmup animation\n" +
                    "instant-teleport: false";
        }
        if (!configStr.contains("disable-animations:")) {
            configStr += "\n\n# Disable the particle animations when teleporting through a beacon\n" +
                    "disable-animations: false";
        }
        if (!configStr.contains("launch-player:")) {
            configStr += "\n\n# Launch the player into the air when teleporting through a beacon\n" +
                    "launch-player: true";
        }
        if (!configStr.contains("launch-player-height:")) {
            configStr += "\n\n# The y-level players will launch into the air before teleporting to the destination. Anti-cheat plugins may not allow\n" +
                    "# players to go above a certain height, so adjust this as needed. The minimum value is the world height.\n" +
                    "launch-player-height: 576";
        }
        if (!configStr.contains("disable-group-teleporting:")) {
            configStr += "\n\n# By default, beacons teleport anyone standing on top of them. If you want to limit the teleportation to only the\n" +
                    "# player who chooses the destination, set this to true.\n" +
                    "disable-group-teleporting: false";
        }
        if (!configStr.contains("allow-beacon-break-by-owner:")) {
            configStr += "\n\n# If the BeaconWaypoints.removeWaypoints permission is disabled for a player, this will still allow them to break\n" +
                    "# a beacon if all waypoints attached to it are owned by them. The owner of a beacon is the player who placed it.\n" +
                    "allow-beacon-break-by-owner: true";
        }
        if (!configStr.contains("payment-mode:")) {
            configStr += "\n\n# If you want to have players pay to teleport, you can change it here.\n" +
                    "# The cost to teleport is determined by the amounts given here multiplied by the distance between the beacons in chunks.\n" +
                    "# The value for each is only used when that mode is specified.\n" +
                    "# If you want the cost to be the same at any distance, then set that value below\n" +
                    "# and set cost-multiplier to 0.\n" +
                    "# If a player is teleporting between dimensions, it will charge the dimension price.\n" +
                    "# Money mode uses the currency from the EssentialsX plugin\n" +
                    "# Modes: none, xp, money\n" +
                    "payment-mode: none";
        }
        if (!configStr.contains("xp-cost-per-chunk:")) {
            configStr += "\n\nxp-cost-per-chunk: 0";
        }
        if (!configStr.contains("xp-cost-dimension:")) {
            configStr += "\n\nxp-cost-dimension: 0";
        }
        if (!configStr.contains("money-cost-per-chunk:")) {
            configStr += "\n\nmoney-cost-per-chunk: 0";
        }
        if (!configStr.contains("money-cost-dimension:")) {
            configStr += "\n\nmoney-cost-dimension: 0";
        }
        if (!configStr.contains("cost-multiplier:")) {
            configStr += "\n\n# This value changes the cost of travel based on distance.\n" +
                    "# The formula used is cost*(distance^multiplier), and the multiplier must be at least 0\n" +
                    "# 0: constant cost for any distance\n" +
                    "# Between 0 and 1: cost increases slower with distance\n" +
                    "# 1: cost increases linearly\n" +
                    "# Above 1: cost increases faster with distance\n" +
                    "# This does not affect the price of teleporting between dimensions.\n" +
                    "cost-multiplier: 0";
        }
        if (!configStr.contains("required-items:")) {
            configStr += "\n\n# Require players to have a specific item with a specific name to teleport.\n" +
                    "# If the item does not need a specific name (ex. custom items), then remove the \"name\" field.\n" +
                    "# The amount of the item the player needs in their inventory can be set with the \"amount\" field.\n" +
                    "# The amount of the item the player needs in their inventory to travel between dimensions can be set with the \"amount-dimension\" field.\n" +
                    "# If you do not want the cost multiplier to affect item amounts needed, set \"use-multiplier\" to false.\n" +
                    "# This could be useful if you require a certain amount of an item but also want an XP payment based on distance.\n" +
                    "# You can set the item(s) to be consumed or let the player keep it with the \"consume\" field (ex. teleport spell item)\n" +
                    "#\n" +
                    "# Example for requiring either an ender pearl that is consumed and uses the cost multiplier\n" +
                    "# or a gold nugget named \"Teleport Token\" with gold text that is not consumed and does not use the cost multiplier:\n" +
                    "#\n" +
                    "# required-items:\n" +
                    "#   - item: ENDER_PEARL\n" +
                    "#     amount: 1\n" +
                    "#     dimension-amount: 10\n" +
                    "#     use-multiplier: true\n" +
                    "#     consume: true\n" +
                    "#   - item: GOLD_NUGGET\n" +
                    "#     name: §6Teleport Token\n" +
                    "#     amount: 1\n" +
                    "#     use-multiplier: false\n" +
                    "#     consume: false\n" +
                    "required-items:";
        }
        if (!configStr.contains("banned-items:")) {
            configStr += "\n\n# Prevent players from teleporting if they have certain items in their inventory\n" +
                    "# Example:\n" +
                    "# banned-items:\n" +
                    "#   - SHULKER_BOX\n" +
                    "#   - TNT\n" +
                    "banned-items:";
        }
        if (!configStr.contains("discovery-mode:")) {
            configStr += "\n\n# Discovery mode will only show public waypoints that have been \"discovered\" by the player.\n" +
                    "# A player discovers a waypoint by interacting with the beacon associated with it\n" +
                    "discovery-mode: false";
        }
        if (!configStr.contains("allow-all-worlds:")) {
            configStr += "\n\n# Allow waypoints to be created in any world\n" +
                    "allow-all-worlds: true";
        }
        if (!configStr.contains("allowed-worlds:")) {
            configStr += "\n\n# List of worlds that allow waypoints if allow-all-worlds is disabled (based on folder name)\n" +
                    "allowed-worlds:\n" +
                    "  - world\n" +
                    "  - world_nether\n" +
                    "  - world_the_end";
        }
        if (!configStr.contains("waypoint-icons:")) {
            configStr += "\n\n# List of items that can be used for waypoint icons\n" +
                    "# The order given here is the same order that will be in the icon picker menu\n" +
                    "waypoint-icons:\n" +
                    "  - APPLE\n" +
                    "  - SHROOMLIGHT\n" +
                    "  - TOTEM_OF_UNDYING\n" +
                    "  - EMERALD\n" +
                    "  - DIAMOND\n" +
                    "  - END_CRYSTAL\n" +
                    "  - LEATHER\n" +
                    "  - FILLED_MAP\n" +
                    "  - SNOW_BLOCK\n" +
                    "  - RED_MUSHROOM\n" +
                    "  - CARROT\n" +
                    "  - GOLDEN_APPLE\n" +
                    "  - CREEPER_HEAD\n" +
                    "  - PRISMARINE_BRICKS\n" +
                    "  - ALLIUM\n" +
                    "  - IRON_PICKAXE\n" +
                    "  - QUARTZ_BRICKS\n" +
                    "  - SKELETON_SKULL\n" +
                    "  - POPPY\n" +
                    "  - PUMPKIN\n" +
                    "  - HONEYCOMB\n" +
                    "  - SEA_LANTERN\n" +
                    "  - BLUE_ICE\n" +
                    "  - PURPUR_BLOCK\n" +
                    "  - ENCHANTING_TABLE\n" +
                    "  - OAK_LOG\n" +
                    "  - WHEAT\n" +
                    "  - RED_BED\n" +
                    "  - ORANGE_TULIP\n" +
                    "  - BLAZE_POWDER\n" +
                    "  - SUGAR_CANE\n" +
                    "  - LAPIS_LAZULI\n" +
                    "  - CHORUS_FRUIT\n" +
                    "  - END_PORTAL_FRAME\n" +
                    "  - ELYTRA\n" +
                    "  - BREWING_STAND\n" +
                    "  - REDSTONE\n" +
                    "  - RED_SAND\n" +
                    "  - END_STONE\n" +
                    "  - CACTUS\n" +
                    "  - WATER_BUCKET\n" +
                    "  - SHULKER_BOX\n" +
                    "  - CHEST\n" +
                    "  - NETHERITE_INGOT\n" +
                    "  - SOUL_SAND\n" +
                    "  - RED_NETHER_BRICKS\n" +
                    "  - MAGMA_BLOCK\n" +
                    "  - SAND\n" +
                    "  - ENDER_PEARL\n" +
                    "  - WARPED_STEM\n" +
                    "  - CRIMSON_STEM\n" +
                    "  - ZOMBIE_HEAD\n" +
                    "  - OBSIDIAN\n" +
                    "  - WITHER_SKELETON_SKULL\n" +
                    "  - GRASS_BLOCK\n" +
                    "  - IRON_BLOCK\n" +
                    "  - COPPER_BLOCK\n" +
                    "  - GOLD_BLOCK\n" +
                    "  - DIAMOND_BLOCK\n" +
                    "  - NETHERITE_BLOCK\n" +
                    "  - SPRUCE_LOG\n" +
                    "  - BIRCH_LOG\n" +
                    "  - JUNGLE_LOG\n" +
                    "  - ACACIA_LOG\n" +
                    "  - DARK_OAK_LOG\n" +
                    "  - SPONGE\n" +
                    "  - BOOKSHELF\n" +
                    "  - NETHERRACK\n" +
                    "  - GLOWSTONE\n" +
                    "  - STONE_BRICKS\n" +
                    "  - DEEPSLATE_BRICKS\n" +
                    "  - MELON\n" +
                    "  - MYCELIUM\n" +
                    "  - EMERALD_BLOCK\n" +
                    "  - HAY_BLOCK\n" +
                    "  - BAMBOO\n" +
                    "  - IRON_BARS\n" +
                    "  - DRAGON_HEAD\n" +
                    "  - CAMPFIRE\n" +
                    "  - BEE_NEST\n" +
                    "  - TNT\n" +
                    "  - BEACON\n" +
                    "  - QUARTZ\n" +
                    "  - IRON_INGOT\n" +
                    "  - COPPER_INGOT\n" +
                    "  - GOLD_INGOT\n" +
                    "  - STRING\n" +
                    "  - FEATHER\n" +
                    "  - GUNPOWDER\n" +
                    "  - WHEAT_SEEDS\n" +
                    "  - BRICK\n" +
                    "  - BOOK\n" +
                    "  - SLIME_BALL\n" +
                    "  - EGG\n" +
                    "  - BONE\n" +
                    "  - BLAZE_ROD\n" +
                    "  - GOLD_NUGGET\n" +
                    "  - NETHER_WART\n" +
                    "  - ENDER_EYE\n" +
                    "  - EXPERIENCE_BOTTLE\n" +
                    "  - NETHER_STAR\n" +
                    "  - FIREWORK_ROCKET\n" +
                    "  - MUSIC_DISC_STAL\n" +
                    "  - CAKE\n" +
                    "  - SWEET_BERRIES\n" +
                    "  - WHITE_WOOL\n" +
                    "  - GOLDEN_PICKAXE\n" +
                    "  - DIAMOND_PICKAXE\n" +
                    "  - NETHERITE_PICKAXE\n" +
                    "  - FISHING_ROD\n" +
                    "  - TRIDENT\n" +
                    "  - GOLDEN_CARROT";
        }

        if (!originalConfigStr.equals(configStr)) {
            Main.getPlugin().getLogger().info("Updated config.yml");
            Writer configFile = new OutputStreamWriter(Files.newOutputStream(configPath), StandardCharsets.UTF_8);
            configFile.write(configStr);
            Objects.requireNonNull(configFile).flush();
            configFile.close();
        }
    }

    /**
     * Checks for the plugin's language.yml file for missing fields and adds the default values
     * @param config
     * @throws IOException
     */
    public static void checkLanguageConfig(FileConfiguration config) throws IOException {
        Path configPath = Paths.get("plugins" + File.separator + "BeaconWaypoints" + File.separator + "language.yml");
        InputStreamReader is = new InputStreamReader(Files.newInputStream(configPath), StandardCharsets.UTF_8);
        String originalConfigStr = new BufferedReader(is).lines().collect(Collectors.joining("\n"));
        String configStr = originalConfigStr;

        if (!configStr.contains("already-exists-at-location:"))
            configStr += "\nalready-exists-at-location: A waypoint already exists at that location";
        if (!configStr.contains("already-shared:"))
            configStr += "\nalready-shared: already has access to";
        if (!configStr.contains("back:"))
            configStr += "\nback: Back";
        if (!configStr.contains("beacon-obstructed:"))
            configStr += "\nbeacon-obstructed: The destination beacon is not able to be traveled to. It either is not constructed correctly, or something is obstructing the beam.";
        if (!configStr.contains("blocks:"))
            configStr += "\nblocks: blocks";
        if (!configStr.contains("cancel:"))
            configStr += "\ncancel: Cancel";
        if (!configStr.contains("cannot-check-for-update:"))
            configStr += "\ncannot-check-for-update: Unable to check for updates";
        if (!configStr.contains("cannot-save-default-language-config:"))
            configStr += "\ncannot-save-default-language-config: Could not save default language config!";
        if (!configStr.contains("config-reloaded:"))
            configStr += "\nconfig-reloaded: BeaconWaypoints config reloaded!";
        if (!configStr.contains("confirm:"))
            configStr += "\nconfirm: Confirm";
        if (!configStr.contains("confirm-delete:"))
            configStr += "\nconfirm-delete: Confirm Delete";
        if (!configStr.contains("change-beacon-effect:"))
            configStr += "\nchange-beacon-effect: Change beacon effect";
        if (!configStr.contains("change-icon:"))
            configStr += "\nchange-icon: Change Icon";
        if (!configStr.contains("click-to-download:"))
            configStr += "\nclick-to-download: Click here to download";
        if (!configStr.contains("cost:"))
            configStr += "\ncost: Cost";
        if (!configStr.contains("created-private-waypoint:"))
            configStr += "\ncreated-private-waypoint: Created the private waypoint";
        if (!configStr.contains("created-public-waypoint:"))
            configStr += "\ncreated-public-waypoint: Created the public waypoint";
        if (!configStr.contains("current-version:"))
            configStr += "\ncurrent-version: Current version";
        if (!configStr.contains("delete-waypoint:"))
            configStr += "\ndelete-waypoint: Delete Waypoint";
        if (!configStr.contains("discovered-waypoint:"))
            configStr += "\ndiscovered-waypoint: Discovered waypoint";
        if (!configStr.contains("distance:"))
            configStr += "\ndistance: Distance";
        if (!configStr.contains("download-link:"))
            configStr += "\ndownload-link: Download link";
        if (!configStr.contains("essentials-not-installed"))
            configStr += "\nessentials-not-installed: EssentialsX is not installed, ignoring Essentials money cost for teleporting.";
        if (!configStr.contains("has-banned-items:"))
            configStr += "\nhas-banned-items: You cannot teleport because you have these items in your inventory";
        if (!configStr.contains("insufficient-items:"))
            configStr += "\ninsufficient-items: You are missing items that are required to teleport";
        if (!configStr.contains("insufficient-money:"))
            configStr += "\ninsufficient-money: You do not have enough money to teleport to that waypoint. Additional money required";
        if (!configStr.contains("insufficient-xp:"))
            configStr += "\ninsufficient-xp: You do not have enough XP to teleport to that waypoint. Additional XP points required";
        if (!configStr.contains("invalid-name:"))
            configStr += "\ninvalid-name: Waypoint names must be 30 characters or fewer";
        if (!configStr.contains("invalid-name-alphanumeric:"))
            configStr += "\ninvalid-name-alphanumeric: Waypoint names must be 30 characters or fewer and can only contain letters, numbers, spaces, underscores, and hyphens.";
        if (!configStr.contains("manage-access:"))
            configStr += "\nmanage-access: Manage access";
        if (!configStr.contains("name-taken:"))
            configStr += "\nname-taken: There is already a public waypoint of that name";
        if (!configStr.contains("new-version-available:"))
            configStr += "\nnew-version-available: A new version of Beacon Waypoints is available!";
        if (!configStr.contains("next-page:"))
            configStr += "\nnext-page: Next Page";
        if (!configStr.contains("no-break-permission:"))
            configStr += "\nno-break-permission: You do not have permission to break beacons that have waypoints set";
        if (!configStr.contains("no-command-permission:"))
            configStr += "\nno-command-permission: You don't have permission to use that command";
        if (!configStr.contains("no-longer-has-access:"))
            configStr += "\nno-longer-has-access: no longer has access to the private waypoint";
        if (!configStr.contains("no-private-waypoint-permission:"))
            configStr += "\nno-private-waypoint-permission: You don't have permission to create private waypoints";
        if (!configStr.contains("no-waypoint-permission:"))
            configStr += "\nno-waypoint-permission: You don't have permission to create waypoints";
        if (!configStr.contains("no-private-waypoints:"))
            configStr += "\nno-private-waypoints: You don't have any private waypoints to share";
        if (!configStr.contains("not-owner:"))
            configStr += "\nnot-owner: Only the owner of this beacon can create a public waypoint";
        if (!configStr.contains("options:"))
            configStr += "\noptions: Options";
        if (!configStr.contains("\nowner:"))
            configStr += "\nowner: Owner";
        if (!configStr.contains("payment-mode-not-found:"))
            configStr += "\npayment-mode-not-found: Payment mode not recognized, defaulting to \"none\"";
        if (!configStr.contains("pinned:"))
            configStr += "\npinned: pinned";
        if (!configStr.contains("pin-waypoint:"))
            configStr += "\npin-waypoint: Pin waypoint";
        if (!configStr.contains("player-not-found:"))
            configStr += "\nplayer-not-found: That player cannot be found or is offline";
        if (!configStr.contains("previous-page:"))
            configStr += "\nprevious-page: Previous Page";
        if (!configStr.contains("private-list-full:"))
            configStr += "\nprivate-list-full: Private waypoint list is full!";
        if (!configStr.contains("private-name-taken:"))
            configStr += "\nprivate-name-taken: There is already a private waypoint of that name";
        if (!configStr.contains("private-waypoint-options:"))
            configStr += "\nprivate-waypoint-options: Options for this private waypoint";
        if (!configStr.contains("private-waypoints:"))
            configStr += "\nprivate-waypoints: Private Waypoints";
        if (!configStr.contains("public-list-full:"))
            configStr += "\npublic-list-full: Public waypoint list is full!";
        if (!configStr.contains("public-waypoint-options:"))
            configStr += "\npublic-waypoint-options: Options for this public waypoint";
        if (!configStr.contains("public-waypoints:"))
            configStr += "\npublic-waypoints: Public Waypoints";
        if (!configStr.contains("click-to-remove-access:"))
            configStr += "\nremove-access: Click to remove access";
        if (!configStr.contains("remove-access:"))
            configStr += "\nremove-access-for-player: Remove access";
        if (!configStr.contains("removed-private-waypoint:"))
            configStr += "\nremoved-private-waypoint: Removed private waypoint";
        if (!configStr.contains("removed-public-waypoint:"))
            configStr += "\nremoved-public-waypoint: Removed public waypoint";
        if (!configStr.contains("required-items:"))
            configStr += "\nrequired-items: Required Items";
        if (!configStr.contains("self-share:"))
            configStr += "\nself-share: You cannot share a private waypoint with yourself";
        if (!configStr.contains("shared-by"))
            configStr += "\nshared-by: Shared by";
        if (!configStr.contains("shared-private-waypoint:"))
            configStr += "\nshared-private-waypoint: private waypoint shared with";
        if (!configStr.contains("stand-on-beacon:"))
            configStr += "\nstand-on-beacon: You must be standing on a beacon to set a waypoint";
        if (!configStr.contains("unpin-waypoint:"))
            configStr += "\nunpin-waypoint: Unpin waypoint";
        if (!configStr.contains("updated-version:"))
            configStr += "\nupdated-version: Updated version";
        if (!configStr.contains("\nwaypoint:"))
            configStr += "\nwaypoint: Waypoint";
        if (!configStr.contains("waypoint-does-not-exist:"))
            configStr += "\nwaypoint-does-not-exist: That waypoint doesn't exist!";
        if (!configStr.contains("waypoint-icon"))
            configStr += "\nwaypoint-icon: Waypoint Icon";
        if (!configStr.contains("waypoint-icon-not-found:"))
            configStr += "\nwaypoint-icon-not-found: Could not add waypoint icon. No item found for";
        if (!configStr.contains("world-not-allowed:"))
            configStr += "\nworld-not-allowed: You cannot set a waypoint in this world";

        if (!originalConfigStr.equals(configStr)) {
            Main.getPlugin().getLogger().info("Updated language.yml");
            Writer configFile = new OutputStreamWriter(Files.newOutputStream(configPath), StandardCharsets.UTF_8);
            configFile.write(configStr);
            Objects.requireNonNull(configFile).flush();
            configFile.close();
            Main.getPlugin().loadLanguage();
        }
    }
}