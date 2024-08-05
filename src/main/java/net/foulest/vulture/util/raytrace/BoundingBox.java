/*
 * Vulture - an advanced anti-cheat plugin designed for Minecraft 1.8.9 servers.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.vulture.util.raytrace;

import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
@ToString
@AllArgsConstructor
@SuppressWarnings("unused")
public class BoundingBox {

    public Vector min;
    public Vector max;

    // Gets the min and max point of a block.
    BoundingBox(@NotNull Block block) {
        IBlockData blockData = ((CraftWorld) block.getWorld()).getHandle().getType(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        net.minecraft.server.v1_8_R3.Block blockNative = blockData.getBlock();

        blockNative.updateShape(((CraftWorld) block.getWorld()).getHandle(), new BlockPosition(block.getX(), block.getY(), block.getZ()));

        min = new Vector(block.getX() + blockNative.B(), block.getY() + blockNative.D(), block.getZ() + blockNative.F());
        max = new Vector(block.getX() + blockNative.C(), block.getY() + blockNative.E(), block.getZ() + blockNative.G());
    }

    // Gets the min and max point of an entity.
    public BoundingBox(Entity entity) {
        AxisAlignedBB box = ((CraftEntity) entity).getHandle().getBoundingBox();
        min = new Vector(box.a, box.b, box.c);
        max = new Vector(box.d, box.e, box.f);
    }

    // Gets the min and max point of an AxisAlignedBB.
    public BoundingBox(@NotNull AxisAlignedBB box) {
        min = new Vector(box.a, box.b, box.c);
        max = new Vector(box.d, box.e, box.f);
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

    private boolean collidesWith(@NotNull BoundingBox box) {
        return (min.getX() <= box.max.getX() && max.getX() >= box.min.getX())
                && (min.getY() <= box.max.getY() && max.getY() >= box.min.getY())
                && (min.getZ() <= box.max.getZ() && max.getZ() >= box.min.getZ());
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

    public List<Block> getCollidingBlocks(Entity entity) {
        List<Block> blocks = new ArrayList<>();
        int blockX = max.getBlockX();

        for (int x = min.getBlockX(); x <= blockX; x++) {
            int blockY = max.getBlockY();

            for (int y = min.getBlockY(); y <= blockY; y++) {
                int blockZ = max.getBlockZ();

                for (int z = min.getBlockZ(); z <= blockZ; z++) {
                    Location loc = new Location(entity.getWorld(), x, y, z);

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

    public static double squareDistanceTo(@NotNull Vector3d origin, @NotNull Vector3d vec) {
        double d0 = vec.x - origin.x;
        double d1 = vec.y - origin.y;
        double d2 = vec.z - origin.z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public static @Nullable Vector3d getIntermediateWithXValue(@NotNull Vector3d origin,
                                                               @NotNull Vector3d vec, double x) {
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

    public static @Nullable Vector3d getIntermediateWithYValue(@NotNull Vector3d origin,
                                                               @NotNull Vector3d vec, double y) {
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

    public static @Nullable Vector3d getIntermediateWithZValue(@NotNull Vector3d origin,
                                                               @NotNull Vector3d vec, double z) {
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

    private boolean isVecInYZ(@NotNull Vector3d vec) {
        return vec.y >= min.getY() && vec.y <= max.getY()
                && vec.z >= min.getZ() && vec.z <= max.getZ();
    }

    private boolean isVecInXZ(@NotNull Vector3d vec) {
        return vec.x >= min.getX() && vec.x <= max.getX()
                && vec.z >= min.getZ() && vec.z <= max.getZ();
    }

    private boolean isVecInXY(@NotNull Vector3d vec) {
        return vec.x >= min.getX() && vec.x <= max.getX()
                && vec.y >= min.getY() && vec.y <= max.getY();
    }

    public boolean isVecInside(@NotNull Vector3d vec) {
        return vec.x > min.getX() && vec.x < max.getX()
                && (vec.y > min.getY() && vec.y < max.getX()
                && vec.z > min.getZ() && vec.z < max.getZ());
    }
}
