package net.foulest.vulture.util.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class to define a 3D area as an axis-aligned bounding box containing some useful transformation methods.
 */
@SuppressWarnings("UnusedReturnValue")
@Getter
@AllArgsConstructor
public class Area {

    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;

    public Area() {
        this(0, 0, 0);
    }

    public Area(double x, double y, double z) {
        set(x, y, z);
    }

    @Contract(pure = true)
    public Area(@NotNull Area other) {
        minX = other.minX;
        minY = other.minY;
        minZ = other.minZ;
        maxX = other.maxX;
        maxY = other.maxY;
        maxZ = other.maxZ;
    }

    public Area contain(@NotNull Area other) {
        return contain(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public Area contain(double x0, double y0, double z0, double x1, double y1, double z1) {
        minX = Math.min(x0, minX);
        minY = Math.min(y0, minY);
        minZ = Math.min(z0, minZ);
        maxX = Math.max(x1, maxX);
        maxY = Math.max(y1, maxY);
        maxZ = Math.max(z1, maxZ);
        return this;
    }

    public Area contain(double x, double y, double z) {
        contain(x, y, z, x, y, z);
        return this;
    }

    public Area set(@NotNull Area other) {
        minX = other.minX;
        minY = other.minY;
        minZ = other.minZ;
        maxX = other.maxX;
        maxY = other.maxY;
        maxZ = other.maxZ;
        return this;
    }

    public Area set(double x, double y, double z) {
        minX = x;
        minY = y;
        minZ = z;
        maxX = x;
        maxY = y;
        maxZ = z;
        return this;
    }

    public Area expand(double x, double y, double z) {
        add(-x, -y, -z, x, y, z);
        return this;
    }

    public void add(double x, double y, double z) {
        add(x, y, z, x, y, z);
    }

    public void add(double x0, double y0, double z0, double x1, double y1, double z1) {
        minX += x0;
        minY += y0;
        minZ += z0;

        maxX += x1;
        maxY += y1;
        maxZ += z1;
    }

    public Area addCoord(double x, double y, double z) {
        double x0 = x > 0 ? 0 : x;
        double y0 = y > 0 ? 0 : y;
        double z0 = z > 0 ? 0 : z;

        double x1 = x > 0 ? x : 0;
        double y1 = y > 0 ? y : 0;
        double z1 = z > 0 ? z : 0;

        add(x0, y0, z0, x1, y1, z1);
        return this;
    }

    public Area interpolate(@NotNull Area destination, int interpolation) {
        minX = interpolate(minX, destination.minX, interpolation);
        maxX = interpolate(maxX, destination.maxX, interpolation);
        minY = interpolate(minY, destination.minY, interpolation);
        maxY = interpolate(maxY, destination.maxY, interpolation);
        minZ = interpolate(minZ, destination.minZ, interpolation);
        maxZ = interpolate(maxZ, destination.maxZ, interpolation);
        return this;
    }

    public Area inner(@NotNull Area other) {
        minX = Math.max(other.minX, minX);
        minY = Math.max(other.minY, minY);
        minZ = Math.max(other.minZ, minZ);
        maxX = Math.min(other.maxX, maxX);
        maxY = Math.min(other.maxY, maxY);
        maxZ = Math.min(other.maxZ, maxZ);
        return this;
    }

    public double distanceX(double x) {
        return x >= minX && x <= maxX ? 0.0 : Math.min(Math.abs(x - minX), Math.abs(x - maxX));
    }

    public double distanceY(double y) {
        return y >= minY && y <= maxY ? 0.0 : Math.min(Math.abs(y - minY), Math.abs(y - maxY));
    }

    public double distanceZ(double z) {
        return z >= minZ && z <= maxZ ? 0.0 : Math.min(Math.abs(z - minZ), Math.abs(z - maxZ));
    }

    public boolean isInside(double x, double y, double z) {
        return x > minX && x < maxX && y > minY && y < maxY && z > minZ && z < maxZ;
    }

    private double interpolate(double value, double destination, int interpolation) {
        return value + (destination - value) / interpolation;
    }
}
