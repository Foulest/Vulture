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
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Utility class to define a custom location containing a position and rotation.
 */
@Data
@AllArgsConstructor
public class CustomLocation {

    private Vector3d pos;
    private Vector2f rot;

    public CustomLocation(double x, double y, double z, float yaw, float pitch) {
        this(new Vector3d(x, y, z), new Vector2f(yaw, pitch));
    }

    private void setPos(double x, double y, double z) {
        if (pos == null) {
            pos = new Vector3d();
        }

        pos.set(x, y, z);
    }

    private void setRot(float yaw, float pitch) {
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

    private void setPos(@NotNull Vector3dc vec) {
        double x = vec.x();
        double y = vec.y();
        double z = vec.z();
        setPos(x, y, z);
    }

    private void setRot(@NotNull Vector2fc vec) {
        float x = vec.x();
        float y = vec.y();
        setRot(x, y);
    }

    public CustomLocation set(@NotNull CustomLocation location) {
        setPos(location.pos != null ? location.pos : new Vector3d());
        setRot(location.rot != null ? location.rot : new Vector2f());
        return this;
    }
}
