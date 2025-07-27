# Beacon Waypoints
### This plugin gives beacons extra functionality by letting players use beacons to fast travel between them! Install this plugin by putting the jar file into your plugins folder and starting/restarting the server.

[Spigot page](https://www.spigotmc.org/resources/beaconwaypoints.99866/)

[CurseForge page](https://www.curseforge.com/minecraft/bukkit-plugins/beaconwaypoints)

Supports versions 1.14-1.20.6

This plugin uses bStats.

## Features
- Create public or private waypoints at beacons
- Configurable icon chooser for waypoints
- Discovery Mode: Players can discover public waypoints by interacting with the beacon and can only teleport between discovered waypoints
- Various payment options for teleportation
- language.yml file for custom translations


## Commands
- /waypoint <name> <public | private> - Create a public or private waypoint for the beacon the player is standing on
- /waypoints reload - Reloads the config
- /waypoint share <player> - share private waypoints with other players


## Demo
https://www.youtube.com/watch?v=-dx9NGfIUa0

[![](http://img.youtube.com/vi/-dx9NGfIUa0/0.jpg)](https://www.youtube.com/watch?v=-dx9NGfIUa0 "Beacon Waypoints Plugin")


Note: Teleportation between beacons requires the beacon to have no blocks inside it, including transparent blocks like glass. Bedrock is an exception, so this plugin will work in the Nether.



## Permissions
- BeaconWaypoints.createWaypoints: Allows players to create waypoints
- BeaconWaypoints.useWaypoints: Allows players to use waypoints
- BeaconWaypoints.usePrivateWaypoints: Allows players to create and teleport to private waypoints
- BeaconWaypoints.breakWaypointBeacons: Allows players to break beacons that have waypoints
- BeaconWaypoints.manageAllWaypoints: Allows players to edit, pin, or remove all public waypoints and corresponding beacons
- BeaconWaypoints.reload: Allows players to reload the config


## Configuration
- max-public-waypoints: The maximum amount of public waypoints that can exist at once on the server (default: 100)
- max-private-waypoints: The maximum amount of private waypoints that each player can have (default: 30)
- force-alphanumeric-names: Force waypoint names to be alphanumeric when they are created
- public-waypoint-menu-rows: The number of rows the public waypoint selection menu will show per page, not including the row for page navigation (default: 3, range: 1-5)
- private-waypoint-menu-rows: The number of rows the private waypoint selection menu will show per page, not including the row for page navigation (default: 2, range: 1-5)
- instant-teleport: Activate teleportation as soon as the destination is chosen without a warmup animation (default: false)
- disable-animations: Disable the particle animations when teleporting through a beacon (default: false)
- launch-player: Launch the player when teleporting through a beacon (default: true)
- launch-player-height: The y-level players will launch into the air before teleporting to the destination. Anti-cheat plugins may not allow players to go above a certain height, so adjust this as needed. The minimum value is the world height. (default: 576)
- disable-group-teleporting: By default, beacons teleport anyone standing on top of them. If you want to limit the teleportation to only the player who chooses the destination, set this to true. (default: false)
- allow-beacon-break-by-owner: If the BeaconWaypoints.breakWaypointBeacons permission is disabled for a player, this will still allow them to break a beacon if all waypoints attached to it are owned by them. The owner of a beacon is the player who placed it. (default: true)
- payment-mode: none, xp, or money (default: none)
- xp-cost-per-chunk: The cost per chunk between two waypoints using the XP payment mode (default: 0)
- xp-cost-dimension: The cost to teleport between dimensions using the XP payment mode (default: 0)
- money-cost-per-chunk: The cost per chunk between two waypoints using the money payment mode (default: 0)
- money-cost-dimension: The cost to teleport between dimensions using the money payment mode (default: 0)
- cost-multiplier: A multiplier that affects the cost based on distance. The formula used is cost*(distance^multiplier) (default: 0)
- required-items: a list of items with different properties regarding payment (default: empty)
- banned-items: a list of items that will not allow players to teleport if they are in their inventory (default: empty)
- discover-mode: Enable discovery mode which only allows players to teleport to waypoints they have discovered by interacting with their beacon (default: false)
- allow-all-worlds: Allow waypoints to be created in any world (default: true)
- allowed-worlds: List of worlds that allow waypoints based on folder name if allow-all-worlds is disabled (default: world, world_nether, world_the_end)
- waypoint-icons: List of items that can be used for waypoint icons, the order given here is the same order that will be in the icon picker menu (default includes 111 items)

Note: If WorldEdit is used to delete a beacon, the waypoint will not be deleted. You will need to manually place back the beacon and break it, or use the setblock and fill commands instead.

### [Issue Tracker](https://github.com/dawson-vilamaa/BeaconWaypoints/issues) - Please report bugs here
### Contact me on Discord: ti.mirro

## Changelog

### 1.7.2
Changes:
- Added support up to 1.21.8

Fixes:
- The player is no longer able to take a beacon from the inventory menu if an error occurs when clicking it

### 1.7.1
Fixes:
- Fixed error when reading player JSON files that were created in a previous version

### 1.7
Changes:
- Added support for 1.20.6
- Added private waypoint sharing
- Usage: /waypoint share <player>
- Shared private waypoints will appear in the other player's private waypoint list
- Remove player access in the waypoint options menu
- Added more checks and default fallbacks for any missing plugin data or config items
- A beacon no longer needs a public waypoint associated with it to open the waypoint menu, though it still requires at least one public or private waypoint. This may change to have no restrictions in a future update so that any beacon can be used to teleport, even if it doesn't have any waypoint set

Fixes:
- Messages are now properly sent to waypoint owner(s) when a beacon with waypoints is broken
- Players are no able to take items from inventory menus if there is an error on a click event

### 1.6.5
Changes:
- Added support for 1.20.4

### 1.6.4
Changes:
- Added support for 1.20.2
- The compass is no longer visible if a multi-page menu only has one page
- Changed the wording of the default message given when a player cannot teleport to make more sense

### 1.6.3
Changes:
- Added support for 1.20 and 1.20.1

### 1.6.2
Changes:
- Added support for 1.19.4

Fixes:
- Fixed an issue with WorldGuard where a waypoint would be deleted if a protected block was broken

### 1.6.1
Changes:
- Added updater for config.yml and language.yml that loads default values if they are not there

### 1.6.0
Note: There have been changes to both config.yml and language.yml. The plugin will use defaults for anything that is not in these files, but it is recommended to take a look at the example/default files to see what changed.

Changes:
- Added support for 1.19.3
- Added Discovery Mode: Players can discover public waypoints by interacting with the beacon and can only teleport between discovered waypoints
- Added config options for payment to teleport
  - XP payment
  - EssentialsX money payment
  - Cost multiplier based on distance
  - Cost for teleporting between dimensions
  - Required items
    - Custom names
    - Optional consuming of items
    - Checks for items in shulker boxes
- Added config option for banned items for players to have in order to teleport
- Added ability to pin public waypoints to the top of the list for players with the BeaconWaypoints.manageAllWaypoints permission
- Waypoint names no longer need to be alphanumeric, but this can be force in the config file

Fixes:
- Fixed minor typos
- Made the back button consistent in all menus

### 1.5.0
Changes:
- Added support for 1.19.1 and 1.19.2 (by changing a single letter lol)

### 1.4.0
Changes:
- Added support for 1.19
- Added language.yml to support custom languages

### 1.3.2

Fixes:
- Fixed major compatibility bug with NoCheatPlus

### 1.3.1

Fixes:
- Added compatibility with NoCheatPlus by adding temporary exemptions when a player is teleporting.
- Added launch-player-height config option to change how high players are launched before teleporting. Anti-cheat plugins might not allow players above a certain height, so this is a way to work around that.

### 1.3.0

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

### 1.2.0

Changes:
- Added support for versions 1.14-1.17.1 and 1.18.2
- Added bStats metric for the total number of waypoints

Bug Fixes:
- config.yml would not be created automatically (bruh moment)
- Players could activate beacon teleportation if they opened the menu, got moved away, and selected a destination

### 1.1.0
Changes:
- Added bStats.
- Added a config option "disable-group-teleporting" that prevents all players standing on a beacon from teleporting at the same time when it was activated, and instead only teleports the player choosing the destination, when enabled.
- Added config reload command (/waypoints reload). This adds the permission BeaconWaypoints.reload which is enabled for operators by default.
- Changed the icon for the "Options for this waypoint" button from a comparator to the icon of the waypoint being interacted with.
- Added an update checker that notifies the console and operators of an update to the plugin.

Bug Fixes:
- A waypoint was not deleted if the beacon was removed with the setblock or fill commands. Changing or deleting blocks using WorldEdit still does not remove the waypoint.
- BeaconWaypoints could conflict with other plugins and make players take fall damage when landing on the destination beacon.