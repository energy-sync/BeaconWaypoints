package com.github.dawsonvilamaa.beaconwaypoint.version;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.UpdateChecker;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class Version_1_15_R1 implements VersionWrapper {

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
        net.minecraft.server.v1_15_R1.Block beaconHandle = ((CraftBlock) beacon).getNMS().getBlock();
        ItemStack itemStackHandle = playerHandle.inventory.getItemInHand();
        MovingObjectPositionBlock blockHitResult = new MovingObjectPositionBlock(blockLocVec3D, EnumDirection.UP, new BlockPosition(blockLocVec3D), true);
        World levelHandle = playerHandle.getWorld();
        ItemActionContext useOnContent = new ItemActionContext(playerHandle, EnumHand.MAIN_HAND, blockHitResult);
        BlockActionContext blockPlaceContext = new BlockActionContext(useOnContent);
        IBlockData blockState = beaconHandle.getPlacedState(blockPlaceContext);
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
        return Arrays.asList(Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK);
    }

    /**
     * Sends a player a message notification for a plugin update
     *
     * @param player
     */
    @Override
    public void sendOpUpdateMessage(Player player) {
        new UpdateChecker(com.github.dawsonvilamaa.beaconwaypoint.Main.plugin, 99866).getVersion(version -> {
            if (!com.github.dawsonvilamaa.beaconwaypoint.Main.plugin.getDescription().getVersion().equals(version)) {
                player.sendMessage(ChatColor.AQUA + "A new version of Beacon Waypoints is available!\n" + ChatColor.YELLOW + "Current version: " + Main.plugin.getDescription().getVersion() + "\nUpdated version: " + version);
                String json = "[{\"text\":\"§b§nClick here to download\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.spigotmc.org/resources/beaconwaypoints.99866/\"}}]";
                PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(json), ChatMessageType.CHAT);
                ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
            }
        });
    }
}
