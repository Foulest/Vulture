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

@CheckInfo(name = "Velocity (B)", type = CheckType.VELOCITY,
        description = "Checks for incorrect horizontal velocity.", experimental = true)
public class VelocityB extends Check {

    private double lastPosX;
    private double lastPosZ;

    private int lastGivenTicks;
    private boolean takenCorrectXZ;

    public VelocityB(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        int nowTicks = playerData.getTotalTicks();
        int givenTicks = playerData.getVelocityXZ().getFirst();
        int tickDiff = nowTicks - givenTicks;

        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);
            Vector3d flyingPosition = flying.getPosition();

            double deltaXZ = (flying.isMoving() ? Math.hypot(flyingPosition.getX() - lastPosX, flyingPosition.getZ() - lastPosZ) : 0.0);
            double takenXZ = playerData.getVelocityXZ().getLast();
            double diffXZ = Math.abs(deltaXZ - takenXZ);

            // Checks the player for exemptions.
            if (playerData.isAgainstBlock()) {
                lastPosX = (flying.isMoving() ? flyingPosition.getX() : lastPosX);
                lastPosZ = (flying.isMoving() ? flyingPosition.getZ() : lastPosZ);
                return;
            }

            if (takenXZ > 0.05875) {
                // Check if player ever took correct velocity
                if (diffXZ < 0.001) {
                    // Player repeating correct velocity
                    if (takenCorrectXZ) {
                        flag(false, "(repeated) dXZ=" + deltaXZ + " vXZ=" + takenXZ + " diffXZ=" + diffXZ + " tDiff=" + tickDiff);
                    }

                    takenCorrectXZ = true;
                    playerData.setTimestamp(ActionType.VELOCITY_TAKEN);
                }

                // Velocity packet sent; flag if player never took correct velocity
                if (lastGivenTicks != givenTicks && tickDiff > 0) {
                    if (!takenCorrectXZ) {
                        flag(false, "dXZ=" + deltaXZ + " vXZ=" + takenXZ + " diffXZ=" + diffXZ + " tDiff=" + tickDiff);
                    }

                    takenCorrectXZ = false;
                    lastGivenTicks = givenTicks;
                }
            }

            lastPosX = (flying.isMoving() ? flyingPosition.getX() : lastPosX);
            lastPosZ = (flying.isMoving() ? flyingPosition.getZ() : lastPosZ);
        }
    }
}
