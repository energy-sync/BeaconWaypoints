package com.github.dawsonvilamaa.beaconwaypoint;

import com.github.dawsonvilamaa.beaconwaypoint.waypoints.WaypointCoord;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MathHelper {
    public static double distance2D(WaypointCoord loc1, WaypointCoord loc2) {
        return distance2D(loc1.getLocation(), loc2.getLocation());
    }

    public static double distance2D(Location loc1, Location loc2) {
        Chunk chunk1 = loc1.getChunk();
        Chunk chunk2 = loc2.getChunk();
        return distance2D(chunk1.getX(), chunk1.getZ(), chunk2.getX(), chunk2.getZ());
    }

    public static double distance2D(double x1, double y1, double x2, double y2) {
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    //thanks to DOGC_Kyle on spigotmc.org for the xp math help

    public static int getXpPoints(Player player){
        int xp = 0;
        int level = player.getLevel();
        xp += getXpAtLevel(level);
        xp += Math.round(getXpToLevelUp(level) * player.getExp());
        return xp;
    }

    public static int getXpAtLevel(int level){
        if (level <= 16)
            return (int) (Math.pow(level, 2) + 6 * level);
        else if (level <= 31)
            return (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360);
        else
            return (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
    }

    public static int getXpToLevelUp(int level){
        if (level <= 15)
            return 2 * level + 7;
        else if (level <= 30)
            return 5 * level - 38;
        else
            return 9 * level - 158;
    }

    public static int setXp(Player player, int exp) {
        int currentXp = getPlayerXp(player);
        player.setExp(0);
        player.setLevel(0);
        int newExp = currentXp + exp;
        player.giveExp(newExp);
        return newExp;
    }

    public static int getPlayerXp(Player player){
        int xp = 0;
        int level = player.getLevel();
        xp += getXpAtLevel(level);
        xp += Math.round(getXpToLevelUp(level) * player.getExp());
        return xp;
    }
}