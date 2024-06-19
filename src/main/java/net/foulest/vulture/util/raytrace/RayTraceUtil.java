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

import lombok.*;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RayTraceUtil {

    public static @Nullable Block getBlockPlayerLookingAt(@NotNull Player player, double distance) {
        RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection());
        List<Vector> positions = rayTrace.traverse(distance, 0.01);

        for (Vector vector : positions) {
            Location position = vector.toLocation(player.getWorld());
            Block block = player.getWorld().getBlockAt(position);

            if (block != null && block.getType() != Material.AIR
                    && rayTrace.intersects(new BoundingBox(block), distance, 0.01)) {
                return block;
            }
        }
        return null;
    }

    /**
     * RayTrace class, taken from SpigotMC.
     * <p>
     * <a href="https://www.spigotmc.org/threads/hitboxes-and-ray-tracing.174358/">...</a>
     */
    @AllArgsConstructor
    public static class RayTrace {

        Vector origin;
        Vector direction;

        // Gets a point on the raytrace at X blocks away
        public Vector getPosition(double blocksAway) {
            return origin.clone().add(direction.clone().multiply(blocksAway));
        }

        // Checks if a position is on contained within the position
        public boolean isOnLine(@NotNull Vector position) {
            double t = (position.getX() - origin.getX()) / direction.getX();
            return position.getBlockY() == origin.getY() + (t * direction.getY())
                    && position.getBlockZ() == origin.getZ() + (t * direction.getZ());
        }

        // Get all positions on a raytrace
        public List<Vector> traverse(double blocksAway, double accuracy) {
            ArrayList<Vector> positions = new ArrayList<>();

            for (double d = 0; d <= blocksAway; d += accuracy) {
                positions.add(getPosition(d));
            }
            return positions;
        }

        // Intersection detection for current raytrace
        public boolean intersects(Vector min, Vector max,
                                  double blocksAway, double accuracy) {
            List<Vector> positions = traverse(blocksAway, accuracy);

            for (Vector position : positions) {
                if (intersects(position, min, max)) {
                    return true;
                }
            }
            return false;
        }

        // BoundingBox instead of Vector
        public boolean intersects(BoundingBox boundingBox, double blocksAway, double accuracy) {
            List<Vector> positions = traverse(blocksAway, accuracy);

            for (Vector position : positions) {
                if (intersects(position, boundingBox.min, boundingBox.max)) {
                    return true;
                }
            }
            return false;
        }

        // General intersection detection
        public static boolean intersects(@NotNull Vector position, @NotNull Vector min, Vector max) {
            if (position.getX() < min.getX() || position.getX() > max.getX()) {
                return false;
            } else if (position.getY() < min.getY() || position.getY() > max.getY()) {
                return false;
            } else {
                return position.getZ() >= min.getZ() && position.getZ() <= max.getZ();
            }
        }

        // Intersection detection for current raytrace with return
        public Vector positionOfIntersect(Vector min, Vector max,
                                          double blocksAway, double accuracy) {
            List<Vector> positions = traverse(blocksAway, accuracy);

            for (Vector position : positions) {
                if (intersects(position, min, max)) {
                    return position;
                }
            }
            return null;
        }

        // BoundingBox instead of Vector
        public Vector positionOfIntersect(BoundingBox boundingBox, double blocksAway, double accuracy) {
            List<Vector> positions = traverse(blocksAway, accuracy);

            for (Vector position : positions) {
                if (intersects(position, boundingBox.min, boundingBox.max)) {
                    return position;
                }
            }
            return null;
        }

        // debug / effects
        public void highlight(World world, double blocksAway, double accuracy) {
            for (Vector position : traverse(blocksAway, accuracy)) {
                world.playEffect(position.toLocation(world), Effect.COLOURED_DUST, 0);
            }
        }
    }
}
