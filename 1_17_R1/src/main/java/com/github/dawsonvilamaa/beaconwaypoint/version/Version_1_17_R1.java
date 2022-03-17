package com.github.dawsonvilamaa.beaconwaypoint.version;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.github.dawsonvilamaa.beaconwaypoint.UpdateChecker;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.EnumHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class Version_1_17_R1 implements VersionWrapper {

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
        net.minecraft.world.level.block.Block beaconHandle = ((CraftBlock) beacon).getNMS().getBlock();
        ItemStack itemStackHandle = playerHandle.getInventory().getItemInHand();
        MovingObjectPositionBlock blockHitResult = new MovingObjectPositionBlock(blockLocVec3D, EnumDirection.b, new BlockPosition(blockLocVec3D), true); //EnumDirection.b: EnumDirection.UP
        BlockActionContext blockPlaceContext = new BlockActionContext(playerHandle, EnumHand.a, itemStackHandle, blockHitResult); //EnumHand.a: EnumHand.MAIN_HAND
        IBlockData blockState = beaconHandle.getPlacedState(blockPlaceContext);
        World levelHandle = playerHandle.getWorld();
        BlockPosition blockPos = new BlockPosition(blockLocVec3D);
        beaconHandle.interact(blockState, levelHandle, blockPos, playerHandle, EnumHand.a, blockHitResult); //EnumHand.a: EnumHand.MAIN_HAND
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
     * Sends a player a message notification for a plugin update
     *
     * @param player
     */
    @Override
    public void sendOpUpdateMessage(Player player) {
        new UpdateChecker(Main.plugin, 99866).getVersion(version -> {
            if (!Main.plugin.getDescription().getVersion().equals(version)) {
                player.sendMessage(ChatColor.AQUA + "A new version of Beacon Waypoints is available!\n" + ChatColor.YELLOW + "Current version: " + Main.plugin.getDescription().getVersion() + "\nUpdated version: " + version);
                String json = "[{\"text\":\"§b§nClick here to download\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.spigotmc.org/resources/beaconwaypoints.99866/\"}}]";
                PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(json), ChatMessageType.a, player.getUniqueId()); //ChatMessageType.a: ChatMessageType.CHAT
                ((CraftPlayer)player).getHandle().b.sendPacket(packet); //b: connection
            }
        });
    }
}
