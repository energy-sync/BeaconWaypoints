# Beacon Waypoints
### This plugin gives beacons extra functionality by letting players use beacons to fast travel between them! Install this plugin by putting the jar file into your plugins folder and starting/restarting the server.

[Spigot page](https://www.spigotmc.org/resources/beaconwaypoints.99866/)

[CurseForge page](https://www.curseforge.com/minecraft/bukkit-plugins/beaconwaypoints)

Supports versions 1.14-1.19.2

This plugin uses bStats.

## Features
- Create public or private waypoints at beacons
- Configurable icon chooser for waypoints
- language.yml file for custom translations


## Commands
- /waypoint <name> <public | private> - Create a public or private waypoint for the beacon the player is standing on
- /waypoints reload - Reloads the config


## Demo
https://www.youtube.com/watch?v=-dx9NGfIUa0

[![](http://img.youtube.com/vi/-dx9NGfIUa0/0.jpg)](https://www.youtube.com/watch?v=-dx9NGfIUa0 "Beacon Waypoints Plugin")


Note: Teleportation between beacons requires the beacon to have no blocks inside it, including transparent blocks like glass. Bedrock is an exception, so this plugin will work in the Nether.



## Permissions
- BeaconWaypoints.createWaypoints: Allows players to create waypoints
- BeaconWaypoints.useWaypoints: Allows players to use waypoints
- BeaconWaypoints.usePrivateWaypoints: Allows players to create and teleport to private waypoints
- BeaconWaypoints.breakWaypointBeacons: Allows players to break beacons that have waypoints
- BeaconWaypoints.manageAllWaypoints: Allows players to edit or remove all public waypoints and corresponding beacons
- BeaconWaypoints.reload: Allows players to reload the config


## Configuration
- max-public-waypoints: The maximum amount of public waypoints that can exist at once on the server (default: 100)
- max-private-waypoints: The maximum amount of private waypoints that each player can have (default: 30)
- public-waypoint-menu-rows: The number of rows the public waypoint selection menu will show per page, not including the row for page navigation (default: 3, range: 1-5)
- private-waypoint-menu-rows: The number of rows the private waypoint selection menu will show per page, not including the row for page navigation (default: 2, range: 1-5)
- instant-teleport: Activate teleportation as soon as the destination is chosen without a warmup animation (default: false)
- disable-animations: Disable the particle animations when teleporting through a beacon (default: false)
- launch-player: Launch the player when teleporting through a beacon (default: true)
- launch-player-height: The y-level players will launch into the air before teleporting to the destination. Anti-cheat plugins may not allow players to go above a certain height, so adjust this as needed. The minimum value is the world height. (default: 576)
- disable-group-teleporting: By default, beacons teleport anyone standing on top of them. If you want to limit the teleportation to only the player who chooses the destination, set this to true. (default: false)
- allow-beacon-break-by-owner: If the BeaconWaypoints.breakWaypointBeacons permission is disabled for a player, this will still allow them to break a beacon if all waypoints attached to it are owned by them. The owner of a beacon is the player who placed it. (default: true)
- allow-all-worlds: Allow waypoints to be created in any world (default: true)
- allowed-worlds: List of worlds that allow waypoints based on folder name if allow-all-worlds is disabled (default: world, world_nether, world_the_end)
- waypoint-icons: List of items that can be used for waypoint icons, the order given here is the same order that will be in the icon picker menu (default includes 111 items)

Note: If WorldEdit is used to delete a beacon, the waypoint will not be deleted. You will need to manually place back the beacon and break it, or use the setblock and fill commands instead.

### [Issue Tracker](https://github.com/dawson-vilamaa/BeaconWaypoints/issues) - Please report bugs here
### Contact me on Discord: energy_sync#9851

## Changelog

# 1.5.0
Changes:
- Added support for 1.19.1 and 1.19.2 (by changing a single letter lol)

# 1.4.0
Changes:
- Added support for 1.19
- Added language.yml to support custom languages

# 1.3.2

Fixes:
- Fixed major compatibility bug with NoCheatPlus

# 1.3.1

Fixes:
- Added compatibility with NoCheatPlus by adding temporary exemptions when a player is teleporting.
- Added launch-player-height config option to change how high players are launched before teleporting. Anti-cheat plugins might not allow players above a certain height, so this is a way to work around that.

# 1.3.0

Changes:
- The default waypoint type is now public, so typing out "public" is no longer required when creating a new waypoint
- Changed permissions
    - Removed waypoint permission
    - Added createWaypoints permission
    - Added useWaypoints permission
    - Added usePrivateWaypoints permission
    - Added breakWaypointBeacons permission
    - Added manageAllWaypoints permission
- Changed config.yml
    - Added launch-player option
    - Added allow-beacon-break-by-owner option
    - Added allow-all-worlds option

Bug Fixes:
- The back arrow player texture for the player skull in the waypoint options menu would not load
- A waypoint would be removed if a beacon became obstructed and then unobstructed

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
