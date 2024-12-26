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
package net.foulest.vulture.event;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.foulest.vulture.data.PlayerData;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class RotationEvent {

    public final WrappedPacketInFlying to;
    public final WrappedPacketInFlying from;

    /**
     * Gets the change in yaw.
     *
     * @return The change in yaw.
     */
    public float getDeltaYaw() {
        float toYaw = to.getYaw();
        float fromYaw = from.getYaw();
        return Math.abs(toYaw - fromYaw);
    }

    /**
     * Gets the change in pitch.
     *
     * @return The change in pitch.
     */
    public float getDeltaPitch() {
        float toPitch = to.getPitch();
        float fromPitch = from.getPitch();
        return Math.abs(toPitch - fromPitch);
    }

    /**
     * Checks if the player is teleporting.
     *
     * @return Whether the player is teleporting.
     */
    public boolean isTeleport(@NotNull PlayerData playerData) {
        Vector3d toPosition = to.getPosition();
        return playerData.isTeleporting(toPosition);
    }
}
