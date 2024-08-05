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
import lombok.ToString;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@ToString
@CheckInfo(name = "Velocity (A)", type = CheckType.VELOCITY,
        description = "Checks for incorrect vertical velocity.", experimental = true)
public class VelocityA extends Check {

    private double lastPosY;
    private int lastGivenTicks;
    private boolean takenCorrectY;

    public VelocityA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        int nowTicks = playerData.getTotalTicks();
        int givenTicks = playerData.getVelocityY().getFirst();
        int tickDiff = nowTicks - givenTicks;

        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);
            Vector3d flyingPosition = flying.getPosition();

            double deltaY = (flying.isMoving() ? flyingPosition.getY() - lastPosY : 0.0);
            double takenY = playerData.getVelocityY().getLast();
            double diffY = Math.abs(deltaY - takenY);

            // Checks the player for exemptions.
            if (playerData.isUnderBlock()) {
                lastPosY = (flying.isMoving() ? flyingPosition.getY() : lastPosY);
                return;
            }

            if (takenY > 0.0) {
                // Check if player ever took correct velocity
                if (diffY < 0.001) {
                    // Player repeating correct velocity
                    if (takenCorrectY) {
                        flag(false, "(repeated) dY=" + deltaY + " vY=" + takenY + " diffY=" + diffY + " tDiff=" + tickDiff);
                    }

                    takenCorrectY = true;
                    playerData.setTimestamp(ActionType.VELOCITY_TAKEN);
                }

                // TODO: This flags:
                //  dY=0.20312687040733124 vY=0.46075

                // Velocity packet sent; flag if player never took correct velocity
                if (lastGivenTicks != givenTicks && tickDiff > 0) {
                    if (!takenCorrectY) {
                        flag(false, "dY=" + deltaY + " vY=" + takenY + " diffY=" + diffY + " tDiff=" + tickDiff);
                    }

                    takenCorrectY = false;
                    lastGivenTicks = givenTicks;
                }
            }

            lastPosY = (flying.isMoving() ? flyingPosition.getY() : lastPosY);
        }
    }
}
