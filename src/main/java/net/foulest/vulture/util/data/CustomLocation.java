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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Utility class to define a custom location containing a position and rotation.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class CustomLocation {

    private Vector3d pos;
    private Vector2f rot;

    public CustomLocation() {
        this(new Vector3d(), new Vector2f());
    }

    public CustomLocation(double x, double y, double z, float yaw, float pitch) {
        this(new Vector3d(x, y, z), new Vector2f(yaw, pitch));
    }

    public void setPos(double x, double y, double z) {
        if (pos == null) {
            pos = new Vector3d();
        }

        pos.set(x, y, z);
    }

    public void setRot(float yaw, float pitch) {
        if (rot == null) {
            rot = new Vector2f();
        }

        rot.set(yaw, pitch);
    }

    public CustomLocation set(double x, double y, double z, float yaw, float pitch) {
        setPos(x, y, z);
        setRot(yaw, pitch);
        return this;
    }

    public void setPos(@NotNull Vector3dc vec) {
        setPos(vec.x(), vec.y(), vec.z());
    }

    public void setRot(@NotNull Vector2fc vec) {
        setRot(vec.x(), vec.y());
    }

    public CustomLocation set(@NotNull CustomLocation location) {
        setPos(location.pos != null ? location.pos : new Vector3d());
        setRot(location.rot != null ? location.rot : new Vector2f());
        return this;
    }

    public boolean hasPos() {
        return pos != null;
    }

    public boolean hasRot() {
        return rot != null;
    }
}
