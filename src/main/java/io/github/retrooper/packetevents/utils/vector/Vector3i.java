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
 * 3D int Vector.
 * This vector can represent coordinates, angles, or anything you want.
 * You can use this to represent an array if you really want.
 * PacketEvents usually uses this for block positions as they don't need any decimals.
 * <p>
 * This is the invalid vector.
 * In wrappers, when a vector is null in the actual packet, PacketEvents will set our high level vector X,Y,Z values
 * to -1 to avoid null pointer exceptions.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class Vector3i implements Cloneable {

    /**
     * X (coordinate/angle/whatever you wish)
     */
    public int x;

    /**
     * Y (coordinate/angle/whatever you wish)
     */
    public int y;

    /**
     * Z (coordinate/angle/whatever you wish)
     */
    public int z;

    /**
     * Default constructor setting all coordinates/angles/values to their default values (=0).
     */
    public Vector3i() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3i(@NotNull Location location) {
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
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
    public Vector3i(int @NotNull [] array) {
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
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Vector3i vec = (Vector3i) obj;
        return x == vec.x && y == vec.y && z == vec.z;
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
    public Vector3i clone() {
        return new Vector3i(x, y, z);
    }
}
