# Beacon Waypoints
### This plugin gives beacons extra functionality by letting players use beacons to fast travel between them! Install this plugin by putting the jar file into your plugins folder and starting/restarting the server.


Supports versions 1.14-1.18.2

This plugin uses bStats.

## Features
- Create public or private waypoints at beacons
- Configurable icon chooser for waypoints


## Commands
- /waypoint <name> <public | private> - Create a public or private waypoint for the beacon the player is standing on
- /waypoints reload - Reloads the config


## Demo
https://www.youtube.com/watch?v=-dx9NGfIUa0

[![](http://img.youtube.com/vi/-dx9NGfIUa0/0.jpg)](https://www.youtube.com/watch?v=-dx9NGfIUa0 "Beacon Waypoints Plugin")


Note: Teleportation between beacons requires the beacon to have no blocks inside it, including transparent blocks like glass. Bedrock is an exception, so this plugin will work in the Nether.



## Permissions
- BeaconWaypoints.waypoint: Allows players to create waypoints
- BeaconWaypoints.reload: Allows players to reload the config


## Configuration
- max-public-waypoints: The maximum amount of public waypoints that can exist at once on the server (default: 100)
- max-private-waypoints: The maximum amount of private waypoints that each player can have
- public-waypoint-menu-rows: The number of rows the public waypoint selection menu will show per page, not including the row for page navigation (default: 3, range: 1-5)
- private-waypoint-menu-rows: The number of rows the private waypoint selection menu will show per page, not including the row for page navigation (default: 2, range: 1-5)
- instant-teleport: Activate teleportation as soon as the destination is chosen without a warmup animation (default: false)
- disable-animations: Disable the particle animations when teleporting through a beacon (default: false)
- disable-group-teleporting: By default, beacons teleport anyone standing on top of them. If you want to limit the teleportation to only the player who chooses the destination, set this to true. (default: false)
- allowed-worlds: List of worlds that allow waypoints based on folder name (default: world, world_nether, world_the_end)
- waypoint-icons: List of items that can be used for waypoint icons, the order given here is the same order that will be in the icon picker menu (default includes 111 items)

<details>
    <summary>Default Config File</summary>
    ```
#
# CONFIGURATION FOR BEACON WAYPOINTS
#

# The maximum amount of public waypoints that can exist at once on the server
max-public-waypoints: 100

# The maximum amount of private waypoints that each player can have
max-private-waypoints: 30

# The number of rows the public waypoint selection menu will show per page, not including the row for page navigation
# Range: 1-5
public-waypoint-menu-rows: 3

# The number of rows the private waypoint selection menu will show per page, not including the row for page navigation
# Range: 1-5
private-waypoint-menu-rows: 2

# Activate teleportation as soon as the destination is chosen without a warmup animation
instant-teleport: false

# Disable the particle animations when teleporting through a beacon
disable-animations: false

# By default, beacons teleport anyone standing on top of them. If you want to limit the teleportation to only the
# player who chooses the destination, set this to true.
disable-group-teleporting: false

# List of worlds that allow waypoints (based on folder name)
allowed-worlds:
- world
- world_nether
- world_the_end

# List of items that can be used for waypoint icons
# The order given here is the same order that will be in the icon picker menu
waypoint-icons:
- APPLE
- SHROOMLIGHT
- TOTEM_OF_UNDYING
- EMERALD
- DIAMOND
- END_CRYSTAL
- LEATHER
- FILLED_MAP
- SNOW_BLOCK
- RED_MUSHROOM
- CARROT
- GOLDEN_APPLE
- CREEPER_HEAD
- PRISMARINE_BRICKS
- ALLIUM
- IRON_PICKAXE
- QUARTZ_BRICKS
- SKELETON_SKULL
- POPPY
- PUMPKIN
- HONEYCOMB
- SEA_LANTERN
- BLUE_ICE
- PURPUR_BLOCK
- ENCHANTING_TABLE
- OAK_LOG
- WHEAT
- RED_BED
- ORANGE_TULIP
- BLAZE_POWDER
- SUGAR_CANE
- LAPIS_LAZULI
- CHORUS_FRUIT
- END_PORTAL_FRAME
- ELYTRA
- BREWING_STAND
- REDSTONE
- RED_SAND
- END_STONE
- CACTUS
- WATER_BUCKET
- SHULKER_BOX
- CHEST
- NETHERITE_INGOT
- SOUL_SAND
- RED_NETHER_BRICKS
- MAGMA_BLOCK
- SAND
- ENDER_PEARL
- WARPED_STEM
- CRIMSON_STEM
- ZOMBIE_HEAD
- OBSIDIAN
- WITHER_SKELETON_SKULL
- GRASS_BLOCK
- IRON_BLOCK
- COPPER_BLOCK
- GOLD_BLOCK
- DIAMOND_BLOCK
- NETHERITE_BLOCK
- SPRUCE_LOG
- BIRCH_LOG
- JUNGLE_LOG
- ACACIA_LOG
- DARK_OAK_LOG
- SPONGE
- BOOKSHELF
- NETHERRACK
- GLOWSTONE
- STONE_BRICKS
- DEEPSLATE_BRICKS
- MELON
- MYCELIUM
- EMERALD_BLOCK
- HAY_BLOCK
- BAMBOO
- IRON_BARS
- DRAGON_HEAD
- CAMPFIRE
- BEE_NEST
- TNT
- BEACON
- QUARTZ
- IRON_INGOT
- COPPER_INGOT
- GOLD_INGOT
- STRING
- FEATHER
- GUNPOWDER
- WHEAT_SEEDS
- BRICK
- BOOK
- SLIME_BALL
- EGG
- BONE
- BLAZE_ROD
- GOLD_NUGGET
- NETHER_WART
- ENDER_EYE
- EXPERIENCE_BOTTLE
- NETHER_STAR
- FIREWORK_ROCKET
- MUSIC_DISC_STAL
- CAKE
- SWEET_BERRIES
- WHITE_WOOL
- GOLDEN_PICKAXE
- DIAMOND_PICKAXE
- NETHERITE_PICKAXE
- FISHING_ROD
- TRIDENT
- GOLDEN_CARROT
    ```
</details>

Note: If WorldEdit is used to delete a beacon, the waypoint will not be deleted. You will need to manually place back the beacon and break it, or use the setblock and fill commands instead.


### [Issue Tracker](https://github.com/dawson-vilamaa/BeaconWaypoints/issues) - Please report bugs here
### Contact me on Discord: energy_sync#9851

## Changelog

# 1.2.0

Changes:
- Added support for versions 1.14-1.17.1 and 1.18.2
- Added bStats metric for the total number of waypoints

Bug Fixes:
- config.yml would not be created automatically (bruh moment)
- Players could activate beacon teleportation if they opened the menu, got moved away, and selected a destination

# 1.1.0
Changes:
- Added bStats.
- Added a config option "disable-group-teleporting" that prevents all players standing on a beacon from teleporting at the same time when it was activated, and instead only teleports the player choosing the destination, when enabled.
- Added config reload command (/waypoints reload). This adds the permission BeaconWaypoints.reload which is enabled for operators by default.
- Changed the icon for the "Options for this waypoint" button from a comparator to the icon of the waypoint being interacted with.
- Added an update checker that notifies the console and operators of an update to the plugin.

Bug Fixes:
- A waypoint was not deleted if the beacon was removed with the setblock or fill commands. Changing or deleting blocks using WorldEdit still does not remove the waypoint.
- BeaconWaypoints could conflict with other plugins and make players take fall damage when landing on the destination beacon.