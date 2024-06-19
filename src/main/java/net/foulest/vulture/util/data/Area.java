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
package net.foulest.vulture.util.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class to define a 3D area as an axis-aligned bounding box containing some useful transformation methods.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@SuppressWarnings("UnusedReturnValue")
public class Area {

    public Vector min;
    public Vector max;

    public Area() {
        this(0, 0, 0);
    }

    public Area(double x, double y, double z) {
        set(x, y, z);
    }

    @Contract(pure = true)
    public Area(@NotNull Area other) {
        min = other.min.clone();
        max = other.max.clone();
    }

    public Area contain(@NotNull Area other) {
        return contain(other.min.getX(), other.min.getY(), other.min.getZ(),
                other.max.getX(), other.max.getY(), other.max.getZ());
    }

    public Area contain(double x0, double y0, double z0, double x1, double y1, double z1) {
        min = new Vector(Math.min(x0, min.getX()), Math.min(y0, min.getY()), Math.min(z0, min.getZ()));
        max = new Vector(Math.max(x1, max.getX()), Math.max(y1, max.getY()), Math.max(z1, max.getZ()));
        return this;
    }

    public Area contain(double x, double y, double z) {
        contain(x, y, z, x, y, z);
        return this;
    }

    public Area set(@NotNull Area other) {
        min = other.min.clone();
        max = other.max.clone();
        return this;
    }

    public Area set(double x, double y, double z) {
        min = new Vector(x, y, z);
        max = new Vector(x, y, z);
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
        min = new Vector(min.getX() + x0, min.getY() + y0, min.getZ() + z0);
        max = new Vector(max.getX() + x1, max.getY() + y1, max.getZ() + z1);
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
        min = new Vector(interpolate(min.getX(), destination.min.getX(), interpolation),
                interpolate(min.getY(), destination.min.getY(), interpolation),
                interpolate(min.getZ(), destination.min.getZ(), interpolation));

        max = new Vector(interpolate(max.getX(), destination.max.getX(), interpolation),
                interpolate(max.getY(), destination.max.getY(), interpolation),
                interpolate(max.getZ(), destination.max.getZ(), interpolation));
        return this;
    }

    public Area inner(@NotNull Area other) {
        min = new Vector(Math.max(other.min.getX(), min.getX()),
                Math.max(other.min.getY(), min.getY()),
                Math.max(other.min.getZ(), min.getZ()));

        max = new Vector(Math.min(other.max.getX(), max.getX()),
                Math.min(other.max.getY(), max.getY()),
                Math.min(other.max.getZ(), max.getZ()));
        return this;
    }

    public double distanceX(double x) {
        return x >= min.getX() && x <= max.getX() ? 0.0 : Math.min(Math.abs(x - min.getX()), Math.abs(x - max.getX()));
    }

    public double distanceY(double y) {
        return y >= min.getY() && y <= max.getY() ? 0.0 : Math.min(Math.abs(y - min.getY()), Math.abs(y - max.getY()));
    }

    public double distanceZ(double z) {
        return z >= min.getZ() && z <= max.getZ() ? 0.0 : Math.min(Math.abs(z - min.getZ()), Math.abs(z - max.getZ()));
    }

    public boolean isInside(double x, double y, double z) {
        return x > min.getX() && x < max.getX() && y > min.getY() && y < max.getY() && z > min.getZ() && z < max.getZ();
    }

    private double interpolate(double value, double destination, int interpolation) {
        return value + (destination - value) / interpolation;
    }
}
