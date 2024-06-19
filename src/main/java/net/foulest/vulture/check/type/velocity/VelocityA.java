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
package net.foulest.vulture.check.type.velocity;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "Velocity (A)", type = CheckType.VELOCITY,
        description = "Checks for incorrect vertical velocity.", experimental = true)
public class VelocityA extends Check {

    private double lastY;

    public VelocityA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        int ticksSinceGiven = playerData.getTicksSince(ActionType.VELOCITY_GIVEN);
        int ticksSinceTaken = playerData.getTicksSince(ActionType.VELOCITY_TAKEN);

        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);
            Vector3d flyingPosition = flying.getPosition();

            double deltaY = (flying.isMoving() ? flyingPosition.getY() - lastY : 0.0);
            double takenVelocity = playerData.getVelocityY();
            double threshold = ticksSinceGiven == 1 ? 0.085615 : 0.003017;

            // Checks the player for exemptions.
            if (playerData.isNearLiquid()
                    || playerData.isUnderBlock()
                    || playerData.isInWeb()
                    || playerData.isOnClimbable()
                    || player.isInsideVehicle()
                    || takenVelocity <= 0.0) {
                if (takenVelocity < 0.0 && deltaY == 0.0 && playerData.isNearGround()) {
                    playerData.setTimestamp(ActionType.VELOCITY_TAKEN);
                }

                lastY = (flying.isMoving() ? flyingPosition.getY() : lastY);
                return;
            }

            // Detects if the player's vertical velocity is incorrect.
            if (deltaY != 0.0 && Math.abs(deltaY - takenVelocity) > threshold) {
                flag(false, "Incorrect Velocity"
                        + " (deltaY=" + deltaY
                        + " takenVelocity=" + takenVelocity
                        + " GIVEN=" + ticksSinceGiven
                        + " TAKEN=" + ticksSinceTaken
                        + " DIFF=" + (ticksSinceTaken - ticksSinceGiven + ")")
                );
            } else if (deltaY != 0.0) {
                playerData.setTimestamp(ActionType.VELOCITY_TAKEN);
            }

            lastY = (flying.isMoving() ? flyingPosition.getY() : lastY);

        } else {
            // Detects if the player is ignoring velocity.
            if (ticksSinceGiven > 2 && playerData.getVelocityY() > 0.0
                    && (ticksSinceTaken - ticksSinceGiven) > 1) {
                flag(false, "Ignored Velocity"
                        + " (GIVEN=" + ticksSinceGiven
                        + " TAKEN=" + ticksSinceTaken
                        + " velY=" + playerData.getVelocityY()
                        + " DIFF=" + (ticksSinceTaken - ticksSinceGiven) + ")");
                playerData.setTimestamp(ActionType.VELOCITY_TAKEN);
            }
        }
    }
}
