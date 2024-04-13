package io.github.retrooper.packetevents.utils.vector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * 3D double Vector.
 * This vector can represent coordinates, angles, or anything you want.
 * You can use this to represent an array if you really want.
 *
 * @author retrooper
 * @since 1.8
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class Vector3d {

    /**
     * This is the invalid vector.
     * In wrappers, when a vector is null in the actual packet, PacketEvents will set our high level vector X,Y,Z values
     * to -1 to avoid null pointer exceptions.
     */
    public static final Vector3d INVALID = new Vector3d(-1, -1, -1);

    /**
     * X (coordinate/angle/whatever you wish)
     */
    public double x;

    /**
     * Y (coordinate/angle/whatever you wish)
     */
    public double y;

    /**
     * Z (coordinate/angle/whatever you wish)
     */
    public double z;

    /**
     * Default constructor setting all coordinates/angles/values to their default values (=0).
     */
    public Vector3d() {
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
    }

    public Vector3d(@NotNull Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }

    /**
     * Constructor allowing you to specify an array.
     * X will be set to the first index of an array(if it exists, otherwise 0).
     * Y will be set to the second index of an array(if it exists, otherwise 0).
     * Z will be set to the third index of an array(if it exists, otherwise 0).
     *
     * @param array Array.
     */
    @Contract(pure = true)
    public Vector3d(double @NotNull [] array) {
        if (array.length > 0) {
            x = array[0];
        } else {
            x = 0;
            y = 0;
            z = 0;
            return;
        }

        if (array.length > 1) {
            y = array[1];
        } else {
            y = 0;
            z = 0;
            return;
        }

        if (array.length > 2) {
            z = array[2];
        } else {
            z = 0;
        }
    }

    /**
     * Is the object we are comparing to equal to us?
     * It must be of type Vector3d or Vector3i and all values must be equal to the values in this class.
     *
     * @param obj Compared object.
     * @return Are they equal?
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vector3d) {
            Vector3d vec = (Vector3d) obj;
            return x == vec.x && y == vec.y && z == vec.z;
        } else if (obj instanceof Vector3f) {
            Vector3f vec = (Vector3f) obj;
            return x == vec.x && y == vec.y && z == vec.z;
        } else if (obj instanceof Vector3i) {
            Vector3i vec = (Vector3i) obj;
            return x == vec.x && y == vec.y && z == vec.z;
        }
        return false;
    }

    /**
     * Simply clone an instance of this class.
     *
     * @return Clone.
     */
    @Override
    public Vector3d clone() {
        return new Vector3d(getX(), getY(), getZ());
    }

    public Vector3d add(@NotNull Vector3d target) {
        Vector3d result = new Vector3d(x, y, z);
        result.x += target.x;
        result.y += target.y;
        result.z += target.z;
        return result;
    }

    public Vector3d subtract(@NotNull Vector3d target) {
        Vector3d result = new Vector3d(x, y, z);
        result.x -= target.x;
        result.y -= target.y;
        result.z -= target.z;
        return result;
    }

    public double distance(Vector3d target) {
        return Math.sqrt(distanceSquared(target));
    }

    public double distanceSquared(@NotNull Vector3d target) {
        double distX = (x - target.x) * (x - target.x);
        double distY = (y - target.y) * (y - target.y);
        double distZ = (z - target.z) * (z - target.z);
        return distX + distY + distZ;
    }
}
