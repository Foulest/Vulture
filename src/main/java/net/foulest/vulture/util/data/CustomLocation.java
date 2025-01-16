/*
 * Vulture - a server protection plugin designed for Minecraft 1.8.9 servers.
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

import com.github.retrooper.packetevents.util.Vector3d;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

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
        pos = new Vector3d(x, y, z);
    }

    private void setRot(float yaw, float pitch) {
        rot = new Vector2f(yaw, pitch);
    }

    public @NotNull CustomLocation set(double x, double y, double z, float yaw, float pitch) {
        setPos(x, y, z);
        setRot(yaw, pitch);
        return this;
    }

    private void setPos(@NotNull Vector3d vec) {
        double x = vec.getX();
        double y = vec.getY();
        double z = vec.getZ();
        setPos(x, y, z);
    }

    private void setRot(@NotNull Vector2f vec) {
        float x = vec.getX();
        float y = vec.getY();
        setRot(x, y);
    }

    public @NotNull CustomLocation set(@NotNull CustomLocation location) {
        setPos(location.pos != null ? location.pos : new Vector3d());
        setRot(location.rot != null ? location.rot : new Vector2f());
        return this;
    }
}
