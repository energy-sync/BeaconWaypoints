package com.github.dawsonvilamaa.beaconwaypoint.version;

import org.bukkit.Bukkit;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityBeacon;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Version_1_21_R5 implements VersionWrapper{
    /**
     * Opens the vanilla beacon GUI for a player
     *
     * @param beacon
     * @param player
     */
    @Override
    public void openBeaconMenu(Block beacon, Player player) {
        Location beaconLoc = beacon.getLocation();
        BlockPosition blockPosition = new BlockPosition(beaconLoc.getBlockX(), beaconLoc.getBlockY(), beaconLoc.getBlockZ());
        EntityPlayer playerHandle = ((CraftPlayer) player).getHandle();
        World levelHandle = playerHandle.ai(); //ai(): getWorld()
        TileEntity beaconTileHandle = levelHandle.c_(blockPosition); //c_(): getBlockEntity()
        Container containerHandle = ((TileEntityBeacon) beaconTileHandle).createMenu(playerHandle.nextContainerCounter(), playerHandle.gs(), playerHandle); //gs(): getInventory()
        playerHandle.a(containerHandle); //a(): initMenu()
        playerHandle.a((TileEntityBeacon) beaconTileHandle); //a(): openMenu()
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

    /**
     * Creates a player head that belongs to a username
     * @param username
     * @return item stack
     */
    @Override
    public ItemStack getPlayerHead(String username) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta == null)
            return head;

        PlayerProfile profile = Bukkit.getServer().createPlayerProfile(username);
        skullMeta.setOwnerProfile(profile);
        head.setItemMeta(skullMeta);
        return head;
    }
}