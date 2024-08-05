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

import io.github.retrooper.packetevents.event.eventtypes.CancellableEvent;
import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.foulest.vulture.data.PlayerData;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class MovementEvent implements CancellableEvent {

    public final PlayerData playerData;
    public final WrappedPacketInFlying to;
    public final WrappedPacketInFlying from;
    public final CancellableNMSPacketEvent event;

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        event.setCancelled(cancelled);
    }

    /**
     * Gets the change in X.
     *
     * @return The change in X.
     */
    private double getDeltaX() {
        return to.getPosition().getX() - from.getPosition().getX();
    }

    /**
     * Gets the change in Y.
     *
     * @return The change in Y.
     */
    public double getDeltaY() {
        return to.getPosition().getY() - from.getPosition().getY();
    }

    /**
     * Gets the change in Z.
     *
     * @return The change in Z.
     */
    private double getDeltaZ() {
        return to.getPosition().getZ() - from.getPosition().getZ();
    }

    /**
     * Gets the change in XZ.
     *
     * @return The change in XZ.
     */
    public double getDeltaXZ() {
        double deltaX = getDeltaX();
        double deltaZ = getDeltaZ();
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }

    /**
     * Checks if the player is teleporting.
     *
     * @return Whether the player is teleporting.
     */
    public boolean isTeleport(@NotNull PlayerData playerData) {
        return playerData.isTeleporting(to.getPosition());
    }

    /**
     * Gets the player's to location.
     *
     * @return The player's to location.
     */
    public Location getToLocation() {
        return new Location(playerData.getPlayer().getWorld(), to.getPosition().getX(),
                to.getPosition().getY(), to.getPosition().getZ());
    }

    /**
     * Gets the player's from location.
     *
     * @return The player's from location.
     */
    public Location getFromLocation() {
        return new Location(playerData.getPlayer().getWorld(), from.getPosition().getX(),
                from.getPosition().getY(), from.getPosition().getZ());
    }
}
