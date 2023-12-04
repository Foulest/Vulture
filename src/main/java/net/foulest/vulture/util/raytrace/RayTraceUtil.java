package net.foulest.vulture.util.raytrace;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
public class RayTraceUtil {

    public static Block getBlockPlayerLookingAt(@NonNull Player player, double distance) {
        RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection());
        ArrayList<Vector> positions = rayTrace.traverse(distance, 0.01);

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

    public static Entity getEntityPlayerLookingAt(@NonNull Player player, double distance) {
        RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection());
        ArrayList<Vector> positions = rayTrace.traverse(distance, 0.01);

        double closestEntityDistance = Double.MAX_VALUE;
        Entity closestEntity = null;

        for (Vector vector : positions) {
            Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(vector.toLocation(player.getWorld()), 0.5, 0.5, 0.5);

            for (Entity entity : nearbyEntities) {
                if (!entity.equals(player)) {
                    BoundingBox entityBoundingBox = new BoundingBox(entity);

                    if (rayTrace.intersects(entityBoundingBox, distance, 0.01)) {
                        double eyeLocDistance = player.getEyeLocation().distance(entity.getLocation());

                        if (eyeLocDistance < closestEntityDistance) {
                            closestEntityDistance = eyeLocDistance;
                            closestEntity = entity;
                        }
                    }
                }
            }
        }
        return closestEntity;
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
        public boolean isOnLine(@NonNull Vector position) {
            double t = (position.getX() - origin.getX()) / direction.getX();
            return position.getBlockY() == origin.getY() + (t * direction.getY())
                    && position.getBlockZ() == origin.getZ() + (t * direction.getZ());
        }

        // Get all positions on a raytrace
        public ArrayList<Vector> traverse(double blocksAway, double accuracy) {
            ArrayList<Vector> positions = new ArrayList<>();

            for (double d = 0; d <= blocksAway; d += accuracy) {
                positions.add(getPosition(d));
            }
            return positions;
        }

        // Intersection detection for current raytrace
        public boolean intersects(@NonNull Vector min, @NonNull Vector max,
                                  double blocksAway, double accuracy) {
            ArrayList<Vector> positions = traverse(blocksAway, accuracy);

            for (Vector position : positions) {
                if (intersects(position, min, max)) {
                    return true;
                }
            }
            return false;
        }

        // BoundingBox instead of Vector
        public boolean intersects(@NonNull BoundingBox boundingBox, double blocksAway, double accuracy) {
            ArrayList<Vector> positions = traverse(blocksAway, accuracy);

            for (Vector position : positions) {
                if (intersects(position, boundingBox.min, boundingBox.max)) {
                    return true;
                }
            }
            return false;
        }

        // General intersection detection
        public static boolean intersects(@NonNull Vector position, @NonNull Vector min, @NonNull Vector max) {
            if (position.getX() < min.getX() || position.getX() > max.getX()) {
                return false;
            } else if (position.getY() < min.getY() || position.getY() > max.getY()) {
                return false;
            } else {
                return position.getZ() >= min.getZ() && position.getZ() <= max.getZ();
            }
        }

        // Intersection detection for current raytrace with return
        public Vector positionOfIntersect(@NonNull Vector min, @NonNull Vector max,
                                          double blocksAway, double accuracy) {
            ArrayList<Vector> positions = traverse(blocksAway, accuracy);

            for (Vector position : positions) {
                if (intersects(position, min, max)) {
                    return position;
                }
            }
            return null;
        }

        // BoundingBox instead of Vector
        public Vector positionOfIntersect(@NonNull BoundingBox boundingBox, double blocksAway, double accuracy) {
            ArrayList<Vector> positions = traverse(blocksAway, accuracy);

            for (Vector position : positions) {
                if (intersects(position, boundingBox.min, boundingBox.max)) {
                    return position;
                }
            }
            return null;
        }

        // debug / effects
        public void highlight(@NonNull World world, double blocksAway, double accuracy) {
            for (Vector position : traverse(blocksAway, accuracy)) {
                world.playEffect(position.toLocation(world), Effect.COLOURED_DUST, 0);
            }
        }
    }
}
