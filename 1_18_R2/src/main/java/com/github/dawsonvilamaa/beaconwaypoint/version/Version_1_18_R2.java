package com.github.dawsonvilamaa.beaconwaypoint.version;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.EnumHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class Version_1_18_R2 implements VersionWrapper {

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
        net.minecraft.world.level.block.Block beaconHandle = ((CraftBlock) beacon).getNMS().b(); //b(): getBlock()
        ItemStack itemStackHandle = playerHandle.fr().f(); //fr().f(): getInventory().getItemInHand()
        MovingObjectPositionBlock blockHitResult = new MovingObjectPositionBlock(blockLocVec3D, EnumDirection.b, new BlockPosition(blockLocVec3D), true); //EnumDirection.b: EnumDirection.UP
        BlockActionContext blockPlaceContext = new BlockActionContext(playerHandle, EnumHand.a, itemStackHandle, blockHitResult); //EnumHand.a: EnumHand.MAIN_HAND
        IBlockData blockState = beaconHandle.a(blockPlaceContext); //a(): getPlacedState()
        World levelHandle = playerHandle.W(); //W(): getWorld()
        BlockPosition blockPos = new BlockPosition(blockLocVec3D);
        beaconHandle.a(blockState, levelHandle, blockPos, playerHandle, EnumHand.a, blockHitResult); //a(): interact(), EnumHand.a: EnumHand.MAIN_HAND
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