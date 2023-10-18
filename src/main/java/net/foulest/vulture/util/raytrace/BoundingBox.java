package net.foulest.vulture.util.raytrace;

import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.vulture.util.EnumFacing;
import net.foulest.vulture.util.MovingObjectPosition;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * BoundingBox class, taken from SpigotMC.
 * This class is impossible to use without NMS.
 * <p>
 * <a href="https://www.spigotmc.org/threads/hitboxes-and-ray-tracing.174358/">...</a>
 */
@Getter
@Setter
@AllArgsConstructor
public class BoundingBox {

    public Vector min;
    public Vector max;

    // Gets the min and max point of a block.
    public BoundingBox(@NonNull Block block) {
        IBlockData blockData = ((CraftWorld) block.getWorld()).getHandle().getType(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        net.minecraft.server.v1_8_R3.Block blockNative = blockData.getBlock();

        blockNative.updateShape(((CraftWorld) block.getWorld()).getHandle(), new BlockPosition(block.getX(), block.getY(), block.getZ()));

        min = new Vector(block.getX() + blockNative.B(), block.getY() + blockNative.D(), block.getZ() + blockNative.F());
        max = new Vector(block.getX() + blockNative.C(), block.getY() + blockNative.E(), block.getZ() + blockNative.G());
    }

    // Gets the min and max point of an entity.
    public BoundingBox(@NonNull Entity entity) {
        AxisAlignedBB bb = ((CraftEntity) entity).getHandle().getBoundingBox();
        min = new Vector(bb.a, bb.b, bb.c);
        max = new Vector(bb.d, bb.e, bb.f);
    }

    // Gets the min and max point of an AxisAlignedBB.
    public BoundingBox(@NonNull AxisAlignedBB bb) {
        min = new Vector(bb.a, bb.b, bb.c);
        max = new Vector(bb.d, bb.e, bb.f);
    }

    // Gets the min and max point of a custom BoundingBox.
    public BoundingBox(double minX, double minY, double minZ,
                       double maxX, double maxY, double maxZ) {
        min = new Vector(minX, minY, minZ);
        max = new Vector(maxX, maxY, maxZ);
    }

    // Gets the mid-point of the bounding box.
    public Vector midPoint() {
        return max.clone().add(min).multiply(0.5);
    }

    public boolean collidesWith(@NonNull BoundingBox other) {
        return (min.getX() <= other.max.getX() && max.getX() >= other.min.getX())
                && (min.getY() <= other.max.getY() && max.getY() >= other.min.getY())
                && (min.getZ() <= other.max.getZ() && max.getZ() >= other.min.getZ());
    }

    public BoundingBox expand(double value) {
        double minX = min.getX() - value;
        double minY = min.getY() - value;
        double minZ = min.getZ() - value;
        double maxX = max.getX() + value;
        double maxY = max.getY() + value;
        double maxZ = max.getZ() + value;
        return new BoundingBox(new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ));
    }

    public BoundingBox expand(double x, double y, double z) {
        double minX = min.getX() - x;
        double minY = min.getY() - y;
        double minZ = min.getZ() - z;
        double maxX = max.getX() + x;
        double maxY = max.getY() + y;
        double maxZ = max.getZ() + z;
        return new BoundingBox(new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ));
    }

    public BoundingBox expandMin(double x, double y, double z) {
        double minX = min.getX() - x;
        double minY = min.getY() - y;
        double minZ = min.getZ() - z;
        return new BoundingBox(new Vector(minX, minY, minZ), max);
    }

    public BoundingBox expandMax(double x, double y, double z) {
        double maxX = max.getX() + x;
        double maxY = max.getY() + y;
        double maxZ = max.getZ() + z;
        return new BoundingBox(min, new Vector(maxX, maxY, maxZ));
    }

    public List<Block> getCollidingBlocks(@NonNull Player player) {
        List<Block> blocks = new ArrayList<>();

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    Location loc = new Location(player.getWorld(), x, y, z);

                    if (loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                        BoundingBox boundingBox = new BoundingBox(loc.getBlock());

                        if (boundingBox.collidesWith(this)) {
                            blocks.add(loc.getBlock());
                        }
                    }
                }
            }
        }
        return blocks;
    }

    public static BoundingBox getEntityBoundingBox(Location location) {
        double f = 0.6 / 2.0;
        double f1 = 1.8;
        return (new BoundingBox(location.getX() - f, location.getY(), location.getZ() - f,
                location.getX() + f, location.getY() + f1, location.getZ() + f));
    }

    public static BoundingBox getEntityBoundingBox(double x, double y, double z) {
        double f = 0.6 / 2.0;
        double f1 = 1.8;
        return (new BoundingBox(x - f, y, z - f, x + f, y + f1, z + f));
    }

    public MovingObjectPosition calculateIntercept(@NonNull Vector3d vecA, @NonNull Vector3d vecB) {
        Vector3d[] vectors = new Vector3d[]{
                getIntermediateWithXValue(vecA, vecB, min.getX()),
                getIntermediateWithXValue(vecA, vecB, max.getX()),
                getIntermediateWithYValue(vecA, vecB, min.getY()),
                getIntermediateWithYValue(vecA, vecB, max.getY()),
                getIntermediateWithZValue(vecA, vecB, min.getZ()),
                getIntermediateWithZValue(vecA, vecB, max.getZ())
        };

        boolean[] validityChecks = new boolean[]{
                isVecInYZ(vectors[0]),
                isVecInYZ(vectors[1]),
                isVecInXZ(vectors[2]),
                isVecInXZ(vectors[3]),
                isVecInXY(vectors[4]),
                isVecInXY(vectors[5])
        };

        Vector3d closest = null;
        int closestIndex = -1;

        for (int i = 0; i < vectors.length; i++) {
            if (validityChecks[i] && (closest == null
                    || squareDistanceTo(vecA, vectors[i]) < squareDistanceTo(vecA, closest))) {
                closest = vectors[i];
                closestIndex = i;
            }
        }

        if (closest == null) {
            return null;
        }

        EnumFacing facing;

        switch (closestIndex) {
            case 0:
                facing = EnumFacing.WEST;
                break;
            case 1:
                facing = EnumFacing.EAST;
                break;
            case 2:
                facing = EnumFacing.DOWN;
                break;
            case 3:
                facing = EnumFacing.UP;
                break;
            case 4:
                facing = EnumFacing.NORTH;
                break;
            case 5:
                facing = EnumFacing.SOUTH;
                break;
            default:
                return null; // Or another default action
        }
        return new MovingObjectPosition(closest, facing);
    }

    public double squareDistanceTo(@NonNull Vector3d origin, @NonNull Vector3d vec) {
        double d0 = vec.x - origin.x;
        double d1 = vec.y - origin.y;
        double d2 = vec.z - origin.z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public Vector3d getIntermediateWithXValue(@NonNull Vector3d origin, @NonNull Vector3d vec, double x) {
        double d0 = vec.x - origin.x;
        double d1 = vec.y - origin.y;
        double d2 = vec.z - origin.z;

        if (d0 * d0 < 1.0000000116860974E-7) {
            return null;
        } else {
            double d3 = (x - origin.x) / d0;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(origin.x + d0 * d3,
                    origin.y + d1 * d3, origin.z + d2 * d3) : null;
        }
    }

    public Vector3d getIntermediateWithYValue(@NonNull Vector3d origin, @NonNull Vector3d vec, double y) {
        double d0 = vec.x - origin.x;
        double d1 = vec.y - origin.y;
        double d2 = vec.z - origin.z;

        if (d1 * d1 < 1.0000000116860974E-7) {
            return null;
        } else {
            double d3 = (y - origin.y) / d1;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(origin.x + d0 * d3, origin.y + d1 * d3, origin.z + d2 * d3) : null;
        }
    }

    public Vector3d getIntermediateWithZValue(@NonNull Vector3d origin, @NonNull Vector3d vec, double z) {
        double d0 = vec.x - origin.x;
        double d1 = vec.y - origin.y;
        double d2 = vec.z - origin.z;

        if (d2 * d2 < 1.0000000116860974E-7) {
            return null;
        } else {
            double d3 = (z - origin.z) / d2;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(origin.x + d0 * d3, origin.y + d1 * d3, origin.z + d2 * d3) : null;
        }
    }

    private boolean isVecInYZ(@NonNull Vector3d vec) {
        return vec.y >= min.getY() && vec.y <= max.getY()
                && vec.z >= min.getZ() && vec.z <= max.getZ();
    }

    private boolean isVecInXZ(@NonNull Vector3d vec) {
        return vec.x >= min.getX() && vec.x <= max.getX()
                && vec.z >= min.getZ() && vec.z <= max.getZ();
    }

    private boolean isVecInXY(@NonNull Vector3d vec) {
        return vec.x >= min.getX() && vec.x <= max.getX()
                && vec.y >= min.getY() && vec.y <= max.getY();
    }

    public boolean isVecInside(@NonNull Vector3d vec) {
        return vec.x > min.getX() && vec.x < max.getX()
                && (vec.y > min.getY() && vec.y < max.getX()
                && vec.z > min.getZ() && vec.z < max.getZ());
    }
}
