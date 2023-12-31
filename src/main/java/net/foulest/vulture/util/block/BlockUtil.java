package net.foulest.vulture.util.block;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.data.ConcurrentStream;
import net.foulest.vulture.util.raytrace.BoundingBox;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
@Setter
public class BlockUtil {

    public static boolean isPlayerInUnloadedChunk(@NonNull Player player) {
        return !player.getLocation().getWorld().isChunkLoaded(player.getLocation().getBlockX() >> 4,
                player.getLocation().getBlockZ() >> 4);
    }

    public static boolean isLocationInUnloadedChunk(@NonNull Location location) {
        return !location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static BoundingBox getPlayerFeetBoundingBox(@NonNull Player player) {
        return new BoundingBox(player)
                .expand(0.0, 0.0, 0.0)
                .expandMin(0.0, 1.3, 0.0)
                .expandMax(0.0, -1.0, 0.0);
    }

    public static BoundingBox getPlayerNearBoundingBox(@NonNull Player player) {
        return new BoundingBox(player)
                .expand(1.0, 0.0, 1.0)
                .expandMin(0.0, 1.2, 0.0)
                .expandMax(0.0, 1.0, 0.0);
    }

    public static BoundingBox getPlayerCustomBoundingBox(@NonNull Player player, double expandXZ,
                                                         double expandMin, double expandMax) {
        return new BoundingBox(player)
                .expand(expandXZ, 0.0, expandXZ)
                .expandMin(0.0, expandMin, 0.0)
                .expandMax(0.0, expandMax, 0.0);
    }

    public static ConcurrentStream<Block> getCollidingBlocks(@NonNull Player player, @NonNull BoundingBox boundingBox) {
        return new ConcurrentStream<>(boundingBox.getCollidingBlocks(player), false);
    }

    public static Block getCollidingBlock(@NonNull Player player) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if (isPlayerInUnloadedChunk(player)) {
            playerData.setInsideBlock(false);
            return null;
        }

        BoundingBox boundingBox = new BoundingBox(player)
                .expand(-0.1, -0.01, -0.1);

        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        for (Block block : collidingBlocks.getCollection()) {
            if (block.getType().isSolid()) {
                playerData.setInsideBlock(true);
                return block;
            }
        }

        playerData.setInsideBlock(false);
        return null;
    }

    private static boolean collidesWithSolid(@NonNull Player player, BoundingBox boundingBox) {
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> block.getType().isSolid()
                || block.getType() == Material.WATER_LILY
                || block.getType() == Material.FLOWER_POT
                || block.getType() == Material.CARPET
                || block.getType() == Material.SNOW
                || block.getType() == Material.SKULL);
    }

    public static List<Block> getIntersectingBlocks(@NonNull Location from, @NonNull Location to, @NonNull Player player) {
        // Create a bounding box representing the player's trajectory
        BoundingBox trajectoryBox = new BoundingBox(
                Math.min(from.getX(), to.getX()),
                Math.min(from.getY(), to.getY()),
                Math.min(from.getZ(), to.getZ()),
                Math.max(from.getX(), to.getX()),
                Math.max(from.getY(), to.getY()),
                Math.max(from.getZ(), to.getZ())
        );

        List<Block> blocks = trajectoryBox.getCollidingBlocks(player);

        // Remove any blocks that are not solid
        blocks.removeIf(block -> !block.getType().isSolid());
        return blocks;
    }

    public static boolean isOnGroundOffset(@NonNull Player player, double offset) {
        if (isPlayerInUnloadedChunk(player)) {
            return true;
        }

        BoundingBox boundingBox = new BoundingBox(player).expand(0.0, 0.0, 0.0)
                .expandMin(0.0, offset, 0.0)
                .expandMax(0.0, -1.0, 0.0);
        return collidesWithSolid(player, boundingBox);
    }

    public static boolean isUnderBlock(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerCustomBoundingBox(player, 0.35, -1.0, 1.0);
        return collidesWithSolid(player, boundingBox);
    }

    public static boolean isAgainstBlock(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerCustomBoundingBox(player, 0.01, 0.0, 0.0);
        return collidesWithSolid(player, boundingBox);
    }

    public static boolean isOnSlab(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerFeetBoundingBox(player);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> block.getType() == Material.STEP
                || block.getType() == Material.WOOD_STEP
                || block.getType() == Material.STONE_SLAB2);
    }

    public static boolean isOnStairs(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerFeetBoundingBox(player);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> block.getType() == Material.WOOD_STAIRS
                || block.getType() == Material.ACACIA_STAIRS
                || block.getType() == Material.BIRCH_WOOD_STAIRS
                || block.getType() == Material.BRICK_STAIRS
                || block.getType() == Material.COBBLESTONE_STAIRS
                || block.getType() == Material.DARK_OAK_STAIRS
                || block.getType() == Material.JUNGLE_WOOD_STAIRS
                || block.getType() == Material.NETHER_BRICK_STAIRS
                || block.getType() == Material.QUARTZ_STAIRS
                || block.getType() == Material.RED_SANDSTONE_STAIRS
                || block.getType() == Material.SANDSTONE_STAIRS
                || block.getType() == Material.SMOOTH_STAIRS
                || block.getType() == Material.SPRUCE_WOOD_STAIRS);
    }

    public static boolean isNearStairs(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerCustomBoundingBox(player, 0.5, 1.3, -1.0);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> block.getType() == Material.WOOD_STAIRS
                || block.getType() == Material.ACACIA_STAIRS
                || block.getType() == Material.BIRCH_WOOD_STAIRS
                || block.getType() == Material.BRICK_STAIRS
                || block.getType() == Material.COBBLESTONE_STAIRS
                || block.getType() == Material.DARK_OAK_STAIRS
                || block.getType() == Material.JUNGLE_WOOD_STAIRS
                || block.getType() == Material.NETHER_BRICK_STAIRS
                || block.getType() == Material.QUARTZ_STAIRS
                || block.getType() == Material.RED_SANDSTONE_STAIRS
                || block.getType() == Material.SANDSTONE_STAIRS
                || block.getType() == Material.SMOOTH_STAIRS
                || block.getType() == Material.SPRUCE_WOOD_STAIRS);
    }

    public static boolean isNearPiston(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerNearBoundingBox(player);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> block.getType() == Material.PISTON_BASE
                || block.getType() == Material.PISTON_EXTENSION
                || block.getType() == Material.PISTON_STICKY_BASE
                || block.getType() == Material.PISTON_MOVING_PIECE);
    }

    public static boolean isNearCactus(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerNearBoundingBox(player);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);
        return collidingBlocks.any(block -> block.getType() == Material.CACTUS);
    }

    public static boolean isInWeb(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerCustomBoundingBox(player, 0.0, 0.0, 0.0);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);
        return collidingBlocks.any(block -> block.getType() == Material.WEB);
    }

    public static boolean isInLiquid(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerCustomBoundingBox(player, 0.0, -0.001, 0.0);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> block.getType() == Material.LAVA
                || block.getType() == Material.STATIONARY_LAVA
                || block.getType() == Material.WATER
                || block.getType() == Material.STATIONARY_WATER);
    }

    public static boolean isNearLiquid(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerCustomBoundingBox(player, 0.1, 1.2, 1.0);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> block.getType() == Material.LAVA
                || block.getType() == Material.STATIONARY_LAVA
                || block.getType() == Material.WATER
                || block.getType() == Material.STATIONARY_WATER);
    }

    public static boolean isOnChest(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerFeetBoundingBox(player);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> block.getType() == Material.CHEST
                || block.getType() == Material.ENDER_CHEST
                || block.getType() == Material.TRAPPED_CHEST);
    }

    public static boolean isOnClimbable(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerCustomBoundingBox(player, 0.0, 0.0, 0.0);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> block.getType() == Material.LADDER
                || block.getType() == Material.VINE);
    }

    public static boolean isNearClimbable(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerCustomBoundingBox(player, 0.6385, 0.0, 0.3);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> block.getType() == Material.LADDER
                || block.getType() == Material.VINE);
    }

    public static boolean isOnSnowLayer(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerFeetBoundingBox(player);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);
        return collidingBlocks.any(block -> block.getType() == Material.SNOW);
    }

    public static boolean isOnIce(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerCustomBoundingBox(player, 0.0, 0.3, 0.0);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> block.getType() == Material.ICE
                || block.getType() == Material.PACKED_ICE);
    }

    public static boolean isOnSoulSand(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerCustomBoundingBox(player, 0.0, 0.0, 0.0);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);
        return collidingBlocks.any(block -> block.getType() == Material.SOUL_SAND);
    }

    public static boolean isNearTrapdoor(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerNearBoundingBox(player);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);

        return collidingBlocks.any(block -> block.getType() == Material.TRAP_DOOR
                || block.getType() == Material.IRON_TRAPDOOR);
    }

    public static boolean isNearFenceGate(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerNearBoundingBox(player);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);
        return collidingBlocks.any(block -> block.getType() == Material.FENCE_GATE);
    }

    public static boolean isNearLilyPad(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerCustomBoundingBox(player, 0.0, 1.2, 1.2);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);
        return collidingBlocks.any(block -> block.getType() == Material.WATER_LILY);
    }

    public static boolean isNearAnvil(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerNearBoundingBox(player);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);
        return collidingBlocks.any(block -> block.getType() == Material.ANVIL);
    }

    public static boolean isNearSlimeBlock(@NonNull Player player) {
        if (isPlayerInUnloadedChunk(player)) {
            return false;
        }

        BoundingBox boundingBox = getPlayerCustomBoundingBox(player, 0.1, 1.3, 0.0);
        ConcurrentStream<Block> collidingBlocks = getCollidingBlocks(player, boundingBox);
        return collidingBlocks.any(block -> block.getType() == Material.SLIME_BLOCK);
    }

    public static void visualizeBoundingBox(@NonNull Player player, @NonNull BoundingBox boundingBox) {
        World world = player.getWorld();
        Effect effect = Effect.COLOURED_DUST;

        // Bottom four corners
        world.playEffect(new Location(world, boundingBox.min.getX(), boundingBox.min.getY(), boundingBox.min.getZ()), effect, 0);
        world.playEffect(new Location(world, boundingBox.min.getX(), boundingBox.min.getY(), boundingBox.max.getZ()), effect, 0);
        world.playEffect(new Location(world, boundingBox.max.getX(), boundingBox.min.getY(), boundingBox.min.getZ()), effect, 0);
        world.playEffect(new Location(world, boundingBox.max.getX(), boundingBox.min.getY(), boundingBox.max.getZ()), effect, 0);

        // Top four corners
        world.playEffect(new Location(world, boundingBox.min.getX(), boundingBox.max.getY(), boundingBox.min.getZ()), effect, 0);
        world.playEffect(new Location(world, boundingBox.min.getX(), boundingBox.max.getY(), boundingBox.max.getZ()), effect, 0);
        world.playEffect(new Location(world, boundingBox.max.getX(), boundingBox.max.getY(), boundingBox.min.getZ()), effect, 0);
        world.playEffect(new Location(world, boundingBox.max.getX(), boundingBox.max.getY(), boundingBox.max.getZ()), effect, 0);
    }

    public static double getBlockFriction(@NonNull Player player) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        return playerData.isOnIce() ? 0.98 : 0.60;
    }
}
