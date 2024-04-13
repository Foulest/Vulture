package io.github.retrooper.packetevents.utils.boundingbox;

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@ToString
@SerializableAs("BoundingBox")
public class BoundingBox implements Cloneable, ConfigurationSerializable {

    private double minX;
    private double minY;
    private double minZ;
    private double maxX;
    private double maxY;
    private double maxZ;

    @Contract("_, _ -> new")
    public static @NotNull BoundingBox of(@NotNull Vector corner1, @NotNull Vector corner2) {
        Validate.notNull(corner1, "Corner1 is null!");
        Validate.notNull(corner2, "Corner2 is null!");
        return new BoundingBox(corner1.getX(), corner1.getY(), corner1.getZ(), corner2.getX(), corner2.getY(), corner2.getZ());
    }

    @Contract("_, _ -> new")
    public static @NotNull BoundingBox of(@NotNull Location corner1, @NotNull Location corner2) {
        Validate.notNull(corner1, "Corner1 is null!");
        Validate.notNull(corner2, "Corner2 is null!");
        Validate.isTrue(Objects.equals(corner1.getWorld(), corner2.getWorld()), "Locations from different worlds!");
        return new BoundingBox(corner1.getX(), corner1.getY(), corner1.getZ(), corner2.getX(), corner2.getY(), corner2.getZ());
    }

    @Contract("_, _ -> new")
    public static @NotNull BoundingBox of(@NotNull Block corner1, @NotNull Block corner2) {
        Validate.notNull(corner1, "Corner1 is null!");
        Validate.notNull(corner2, "Corner2 is null!");
        Validate.isTrue(Objects.equals(corner1.getWorld(), corner2.getWorld()), "Blocks from different worlds!");
        int x1 = corner1.getX();
        int y1 = corner1.getY();
        int z1 = corner1.getZ();
        int x2 = corner2.getX();
        int y2 = corner2.getY();
        int z2 = corner2.getZ();
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2) + 1;
        int maxY = Math.max(y1, y2) + 1;
        int maxZ = Math.max(z1, z2) + 1;
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Contract("_ -> new")
    public static @NotNull BoundingBox of(@NotNull Block block) {
        Validate.notNull(block, "Block is null!");
        return new BoundingBox(block.getX(), block.getY(), block.getZ(), (block.getX() + 1), (block.getY() + 1), (block.getZ() + 1));
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull BoundingBox of(@NotNull Vector center, double x, double y, double z) {
        Validate.notNull(center, "Center is null!");
        return new BoundingBox(center.getX() - x, center.getY() - y, center.getZ() - z, center.getX() + x, center.getY() + y, center.getZ() + z);
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull BoundingBox of(@NotNull Location center, double x, double y, double z) {
        Validate.notNull(center, "Center is null!");
        return new BoundingBox(center.getX() - x, center.getY() - y, center.getZ() - z, center.getX() + x, center.getY() + y, center.getZ() + z);
    }

    public BoundingBox() {
        this.resize(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    public BoundingBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.resize(x1, y1, z1, x2, y2, z2);
    }

    public BoundingBox resize(double x1, double y1, double z1, double x2, double y2, double z2) {
        NumberConversions.checkFinite(x1, "x1 not finite");
        NumberConversions.checkFinite(y1, "y1 not finite");
        NumberConversions.checkFinite(z1, "z1 not finite");
        NumberConversions.checkFinite(x2, "x2 not finite");
        NumberConversions.checkFinite(y2, "y2 not finite");
        NumberConversions.checkFinite(z2, "z2 not finite");
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
        return this;
    }

    public Vector getMin() {
        return new Vector(this.minX, this.minY, this.minZ);
    }

    public Vector getMax() {
        return new Vector(this.maxX, this.maxY, this.maxZ);
    }

    public double getWidthX() {
        return this.maxX - this.minX;
    }

    public double getWidthZ() {
        return this.maxZ - this.minZ;
    }

    public double getHeight() {
        return this.maxY - this.minY;
    }

    public double getVolume() {
        return this.getHeight() * this.getWidthX() * this.getWidthZ();
    }

    public double getCenterX() {
        return this.minX + this.getWidthX() * 0.5D;
    }

    public double getCenterY() {
        return this.minY + this.getHeight() * 0.5D;
    }

    public double getCenterZ() {
        return this.minZ + this.getWidthZ() * 0.5D;
    }

    public Vector getCenter() {
        return new Vector(this.getCenterX(), this.getCenterY(), this.getCenterZ());
    }

    public BoundingBox copy(@NotNull BoundingBox other) {
        Validate.notNull(other, "Other bounding box is null!");
        return this.resize(other.getMinX(), other.getMinY(), other.getMinZ(), other.getMaxX(), other.getMaxY(), other.getMaxZ());
    }

    public BoundingBox expand(double negativeX, double negativeY, double negativeZ, double positiveX, double positiveY, double positiveZ) {
        if (negativeX == 0.0D && negativeY == 0.0D && negativeZ == 0.0D && positiveX == 0.0D && positiveY == 0.0D && positiveZ == 0.0D) {
            return this;
        } else {
            double newMinX = this.minX - negativeX;
            double newMinY = this.minY - negativeY;
            double newMinZ = this.minZ - negativeZ;
            double newMaxX = this.maxX + positiveX;
            double newMaxY = this.maxY + positiveY;
            double newMaxZ = this.maxZ + positiveZ;
            double centerZ;

            if (newMinX > newMaxX) {
                centerZ = this.getCenterX();

                if (newMaxX >= centerZ) {
                    newMinX = newMaxX;
                } else if (newMinX <= centerZ) {
                    newMaxX = newMinX;
                } else {
                    newMinX = centerZ;
                    newMaxX = centerZ;
                }
            }

            if (newMinY > newMaxY) {
                centerZ = this.getCenterY();

                if (newMaxY >= centerZ) {
                    newMinY = newMaxY;
                } else if (newMinY <= centerZ) {
                    newMaxY = newMinY;
                } else {
                    newMinY = centerZ;
                    newMaxY = centerZ;
                }
            }

            if (newMinZ > newMaxZ) {
                centerZ = this.getCenterZ();

                if (newMaxZ >= centerZ) {
                    newMinZ = newMaxZ;
                } else if (newMinZ <= centerZ) {
                    newMaxZ = newMinZ;
                } else {
                    newMinZ = centerZ;
                    newMaxZ = centerZ;
                }
            }
            return this.resize(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
        }
    }

    public BoundingBox expand(double x, double y, double z) {
        return this.expand(x, y, z, x, y, z);
    }

    public BoundingBox expand(@NotNull Vector expansion) {
        Validate.notNull(expansion, "Expansion is null!");
        double x = expansion.getX();
        double y = expansion.getY();
        double z = expansion.getZ();
        return this.expand(x, y, z, x, y, z);
    }

    public BoundingBox expand(double expansion) {
        return this.expand(expansion, expansion, expansion, expansion, expansion, expansion);
    }

    public BoundingBox expand(double dirX, double dirY, double dirZ, double expansion) {
        if (expansion == 0.0D) {
            return this;
        } else if (dirX == 0.0D && dirY == 0.0D && dirZ == 0.0D) {
            return this;
        } else {
            double negativeX = dirX < 0.0D ? -dirX * expansion : 0.0D;
            double negativeY = dirY < 0.0D ? -dirY * expansion : 0.0D;
            double negativeZ = dirZ < 0.0D ? -dirZ * expansion : 0.0D;
            double positiveX = dirX > 0.0D ? dirX * expansion : 0.0D;
            double positiveY = dirY > 0.0D ? dirY * expansion : 0.0D;
            double positiveZ = dirZ > 0.0D ? dirZ * expansion : 0.0D;
            return this.expand(negativeX, negativeY, negativeZ, positiveX, positiveY, positiveZ);
        }
    }

    public BoundingBox expand(@NotNull Vector direction, double expansion) {
        return this.expand(direction.getX(), direction.getY(), direction.getZ(), expansion);
    }

    public BoundingBox expand(@NotNull BlockFace blockFace, double expansion) {
        return blockFace == BlockFace.SELF ? this : this.expand(
                new Vector(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()), expansion);
    }

    public BoundingBox expandDirectional(double dirX, double dirY, double dirZ) {
        return this.expand(dirX, dirY, dirZ, 1.0D);
    }

    public BoundingBox expandDirectional(@NotNull Vector direction) {
        return this.expand(direction.getX(), direction.getY(), direction.getZ(), 1.0D);
    }

    public BoundingBox union(double posX, double posY, double posZ) {
        double newMinX = Math.min(this.minX, posX);
        double newMinY = Math.min(this.minY, posY);
        double newMinZ = Math.min(this.minZ, posZ);
        double newMaxX = Math.max(this.maxX, posX);
        double newMaxY = Math.max(this.maxY, posY);
        double newMaxZ = Math.max(this.maxZ, posZ);
        return newMinX == this.minX && newMinY == this.minY && newMinZ == this.minZ
                && newMaxX == this.maxX && newMaxY == this.maxY && newMaxZ == this.maxZ
                ? this : this.resize(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    public BoundingBox union(@NotNull Vector position) {
        return this.union(position.getX(), position.getY(), position.getZ());
    }

    public BoundingBox union(@NotNull Location position) {
        return this.union(position.getX(), position.getY(), position.getZ());
    }

    public BoundingBox union(@NotNull BoundingBox other) {
        if (this.contains(other)) {
            return this;
        } else {
            double newMinX = Math.min(this.minX, other.minX);
            double newMinY = Math.min(this.minY, other.minY);
            double newMinZ = Math.min(this.minZ, other.minZ);
            double newMaxX = Math.max(this.maxX, other.maxX);
            double newMaxY = Math.max(this.maxY, other.maxY);
            double newMaxZ = Math.max(this.maxZ, other.maxZ);
            return this.resize(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
        }
    }

    public BoundingBox intersection(@NotNull BoundingBox other) {
        Validate.isTrue(this.overlaps(other), "The bounding boxes do not overlap!");
        double newMinX = Math.max(this.minX, other.minX);
        double newMinY = Math.max(this.minY, other.minY);
        double newMinZ = Math.max(this.minZ, other.minZ);
        double newMaxX = Math.min(this.maxX, other.maxX);
        double newMaxY = Math.min(this.maxY, other.maxY);
        double newMaxZ = Math.min(this.maxZ, other.maxZ);
        return this.resize(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    public BoundingBox shift(double shiftX, double shiftY, double shiftZ) {
        return shiftX == 0.0D && shiftY == 0.0D && shiftZ == 0.0D ? this
                : this.resize(this.minX + shiftX, this.minY + shiftY, this.minZ + shiftZ,
                this.maxX + shiftX, this.maxY + shiftY, this.maxZ + shiftZ);
    }

    public BoundingBox shift(@NotNull Vector shift) {
        return this.shift(shift.getX(), shift.getY(), shift.getZ());
    }

    public BoundingBox shift(@NotNull Location shift) {
        return this.shift(shift.getX(), shift.getY(), shift.getZ());
    }

    private boolean overlaps(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.minX < maxX && this.maxX > minX
                && this.minY < maxY && this.maxY > minY
                && this.minZ < maxZ && this.maxZ > minZ;
    }

    public boolean overlaps(@NotNull BoundingBox other) {
        return this.overlaps(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public boolean overlaps(@NotNull Vector min, @NotNull Vector max) {
        double x1 = min.getX();
        double y1 = min.getY();
        double z1 = min.getZ();
        double x2 = max.getX();
        double y2 = max.getY();
        double z2 = max.getZ();
        return this.overlaps(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    public boolean contains(double x, double y, double z) {
        return x >= this.minX && x < this.maxX
                && y >= this.minY && y < this.maxY
                && z >= this.minZ && z < this.maxZ;
    }

    public boolean contains(@NotNull Vector position) {
        return this.contains(position.getX(), position.getY(), position.getZ());
    }

    private boolean contains(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.minX <= minX && this.maxX >= maxX
                && this.minY <= minY && this.maxY >= maxY
                && this.minZ <= minZ && this.maxZ >= maxZ;
    }

    public boolean contains(@NotNull BoundingBox other) {
        return this.contains(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public boolean contains(@NotNull Vector min, @NotNull Vector max) {
        double x1 = min.getX();
        double y1 = min.getY();
        double z1 = min.getZ();
        double x2 = max.getX();
        double y2 = max.getY();
        double z2 = max.getZ();
        return this.contains(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    public RayTraceResult rayTrace(@NotNull Vector start, @NotNull Vector direction, double maxDistance) {
        NumberConversions.checkFinite(start.getX(), "Start X is not finite");
        NumberConversions.checkFinite(start.getY(), "Start Y is not finite");
        NumberConversions.checkFinite(start.getZ(), "Start Z is not finite");
        NumberConversions.checkFinite(direction.getX(), "Direction X is not finite");
        NumberConversions.checkFinite(direction.getY(), "Direction Y is not finite");
        NumberConversions.checkFinite(direction.getZ(), "Direction Z is not finite");
        Validate.isTrue(direction.lengthSquared() > 0.0D, "Direction's magnitude is 0!");

        if (maxDistance < 0.0D) {
            return null;
        } else {
            double startX = start.getX();
            double startY = start.getY();
            double startZ = start.getZ();
            Vector normalizedZeroDir = direction.clone();

            if (normalizedZeroDir.getX() == -0.0D) {
                normalizedZeroDir.setX(0.0D);
            }

            if (normalizedZeroDir.getY() == -0.0D) {
                normalizedZeroDir.setY(0.0D);
            }

            if (normalizedZeroDir.getZ() == -0.0D) {
                normalizedZeroDir.setZ(0.0D);
            }

            Vector dir = normalizedZeroDir.normalize();
            double dirX = dir.getX();
            double dirY = dir.getY();
            double dirZ = dir.getZ();
            double divX = 1.0D / dirX;
            double divY = 1.0D / dirY;
            double divZ = 1.0D / dirZ;
            double tMin;
            double tMax;
            BlockFace hitBlockFaceMin;
            BlockFace hitBlockFaceMax;

            if (dirX >= 0.0D) {
                tMin = (this.minX - startX) * divX;
                tMax = (this.maxX - startX) * divX;
                hitBlockFaceMin = BlockFace.WEST;
                hitBlockFaceMax = BlockFace.EAST;
            } else {
                tMin = (this.maxX - startX) * divX;
                tMax = (this.minX - startX) * divX;
                hitBlockFaceMin = BlockFace.EAST;
                hitBlockFaceMax = BlockFace.WEST;
            }

            double tyMin;
            double tyMax;
            BlockFace hitBlockFaceYMin;
            BlockFace hitBlockFaceYMax;

            if (dirY >= 0.0D) {
                tyMin = (this.minY - startY) * divY;
                tyMax = (this.maxY - startY) * divY;
                hitBlockFaceYMin = BlockFace.DOWN;
                hitBlockFaceYMax = BlockFace.UP;
            } else {
                tyMin = (this.maxY - startY) * divY;
                tyMax = (this.minY - startY) * divY;
                hitBlockFaceYMin = BlockFace.UP;
                hitBlockFaceYMax = BlockFace.DOWN;
            }

            if (!(tMin > tyMax) && !(tMax < tyMin)) {
                if (tyMin > tMin) {
                    tMin = tyMin;
                    hitBlockFaceMin = hitBlockFaceYMin;
                }

                if (tyMax < tMax) {
                    tMax = tyMax;
                    hitBlockFaceMax = hitBlockFaceYMax;
                }

                double tzMin;
                double tzMax;
                BlockFace hitBlockFaceZMin;
                BlockFace hitBlockFaceZMax;

                if (dirZ >= 0.0D) {
                    tzMin = (this.minZ - startZ) * divZ;
                    tzMax = (this.maxZ - startZ) * divZ;
                    hitBlockFaceZMin = BlockFace.NORTH;
                    hitBlockFaceZMax = BlockFace.SOUTH;
                } else {
                    tzMin = (this.maxZ - startZ) * divZ;
                    tzMax = (this.minZ - startZ) * divZ;
                    hitBlockFaceZMin = BlockFace.SOUTH;
                    hitBlockFaceZMax = BlockFace.NORTH;
                }

                if (!(tMin > tzMax) && !(tMax < tzMin)) {
                    if (tzMin > tMin) {
                        tMin = tzMin;
                        hitBlockFaceMin = hitBlockFaceZMin;
                    }

                    if (tzMax < tMax) {
                        tMax = tzMax;
                        hitBlockFaceMax = hitBlockFaceZMax;
                    }

                    if (tMax < 0.0D) {
                        return null;
                    } else if (tMin > maxDistance) {
                        return null;
                    } else {
                        double t;
                        BlockFace hitBlockFace;

                        if (tMin < 0.0D) {
                            t = tMax;
                            hitBlockFace = hitBlockFaceMax;
                        } else {
                            t = tMin;
                            hitBlockFace = hitBlockFaceMin;
                        }

                        Vector hitPosition = dir.multiply(t).add(start);
                        return new RayTraceResult(hitPosition, hitBlockFace);
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    public int hashCode() {
        int result = 31 + Double.hashCode(this.maxX);
        result = 31 * result + Double.hashCode(this.maxY);
        result = 31 * result + Double.hashCode(this.maxZ);
        result = 31 * result + Double.hashCode(this.minX);
        result = 31 * result + Double.hashCode(this.minY);
        result = 31 * result + Double.hashCode(this.minZ);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof BoundingBox)) {
            return false;
        } else {
            BoundingBox other = (BoundingBox) obj;

            if (Double.doubleToLongBits(this.maxX) != Double.doubleToLongBits(other.maxX)) {
                return false;
            } else if (Double.doubleToLongBits(this.maxY) != Double.doubleToLongBits(other.maxY)) {
                return false;
            } else if (Double.doubleToLongBits(this.maxZ) != Double.doubleToLongBits(other.maxZ)) {
                return false;
            } else if (Double.doubleToLongBits(this.minX) != Double.doubleToLongBits(other.minX)) {
                return false;
            } else if (Double.doubleToLongBits(this.minY) != Double.doubleToLongBits(other.minY)) {
                return false;
            } else {
                return Double.doubleToLongBits(this.minZ) == Double.doubleToLongBits(other.minZ);
            }
        }
    }

    public BoundingBox clone() {
        try {
            return (BoundingBox) super.clone();
        } catch (CloneNotSupportedException var2) {
            throw new Error(var2);
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("minX", this.minX);
        result.put("minY", this.minY);
        result.put("minZ", this.minZ);
        result.put("maxX", this.maxX);
        result.put("maxY", this.maxY);
        result.put("maxZ", this.maxZ);
        return result;
    }

    @Contract("_ -> new")
    public static @NotNull BoundingBox deserialize(@NotNull Map<String, Object> args) {
        double minX = 0.0D;
        double minY = 0.0D;
        double minZ = 0.0D;
        double maxX = 0.0D;
        double maxY = 0.0D;
        double maxZ = 0.0D;

        if (args.containsKey("minX")) {
            minX = ((Number) args.get("minX")).doubleValue();
        }

        if (args.containsKey("minY")) {
            minY = ((Number) args.get("minY")).doubleValue();
        }

        if (args.containsKey("minZ")) {
            minZ = ((Number) args.get("minZ")).doubleValue();
        }

        if (args.containsKey("maxX")) {
            maxX = ((Number) args.get("maxX")).doubleValue();
        }

        if (args.containsKey("maxY")) {
            maxY = ((Number) args.get("maxY")).doubleValue();
        }

        if (args.containsKey("maxZ")) {
            maxZ = ((Number) args.get("maxZ")).doubleValue();
        }
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
