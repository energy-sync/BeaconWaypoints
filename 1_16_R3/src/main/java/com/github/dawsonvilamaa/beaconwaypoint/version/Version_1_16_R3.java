package com.github.dawsonvilamaa.beaconwaypoint.version;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.UpdateChecker;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class Version_1_16_R3 implements VersionWrapper {

    /**
     * Opens the vanilla beacon GUI for a player
     *
     * @param beacon
     * @param player
     */
    @Override
    public void openBeaconMenu(Block beacon, Player player) {
        Location beaconLoc = beacon.getLocation();
        Vec3D blockLocVec3D = new Vec3D(beaconLoc.getBlockX(), beaconLoc.getBlockY(), beaconLoc.getBlockZ());
        EntityPlayer playerHandle = ((CraftPlayer) player).getHandle();
        net.minecraft.server.v1_16_R3.Block beaconHandle = ((CraftBlock) beacon).getNMS().getBlock();
        ItemStack itemStackHandle = playerHandle.inventory.getItemInHand();
        MovingObjectPositionBlock blockHitResult = new MovingObjectPositionBlock(blockLocVec3D, EnumDirection.UP, new BlockPosition(blockLocVec3D), true);
        BlockActionContext blockPlaceContext = new BlockActionContext(playerHandle, EnumHand.MAIN_HAND, itemStackHandle, blockHitResult);
        IBlockData blockState = beaconHandle.getPlacedState(blockPlaceContext);
        World levelHandle = playerHandle.getWorld();
        BlockPosition blockPos = new BlockPosition(blockLocVec3D);
        beaconHandle.interact(blockState, levelHandle, blockPos, playerHandle, EnumHand.MAIN_HAND, blockHitResult);
    }

    /**
     * Returns the blocks available to be used in a beacon pyramid
     *
     * @return
     */
    @Override
    public List<Material> getPyramidBlocks() {
        return Arrays.asList(Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK, Material.NETHERITE_BLOCK);
    }
}