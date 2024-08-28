/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
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
package io.github.retrooper.packetevents.utils.vector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
public class Vector3d implements Cloneable {

    public double x;
    public double y;
    public double z;

    /**
     * Default constructor setting all coordinates/angles/values to their default values (=0).
     */
    public Vector3d() {
        x = 0.0;
        y = 0.0;
        z = 0.0;
    }

    public Vector3d(@NotNull Location location) {
        x = location.getX();
        y = location.getY();
        z = location.getZ();
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

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    /**
     * Simply clone an instance of this class.
     *
     * @return Clone.
     */
    @Override
    public Vector3d clone() {
        return new Vector3d(x, y, z);
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

    public Vector3d multiply(double target) {
        Vector3d result = new Vector3d(x, y, z);
        result.x *= target;
        result.y *= target;
        result.z *= target;
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

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
