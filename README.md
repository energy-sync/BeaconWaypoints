# Beacon Waypoints
### This plugin gives beacons extra functionality by letting players use beacons to fast travel between them! Install this plugin by putting the jar file into your plugins folder and starting/restarting the server.


Developed and tested for version 1.18.1

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

Note: If WorldEdit is used to delete a beacon, the waypoint will not be deleted. You will need to manually place back the beacon and break it, or use the setblock and fill commands instead.


### [Issue Tracker](https://github.com/dawson-vilamaa/BeaconWaypoints/issues) - Please report bugs here
### Contact me on Discord: energy_sync#9851

## Changelog

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