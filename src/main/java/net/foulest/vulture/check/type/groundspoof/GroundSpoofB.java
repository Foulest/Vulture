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
package net.foulest.vulture.check.type.groundspoof;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import lombok.ToString;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@ToString
@CheckInfo(name = "GroundSpoof (B)", type = CheckType.GROUNDSPOOF,
        description = "Detects clients sending invalid on-ground Flying packets.")
public class GroundSpoofB extends Check {

    private static final double ON_GROUND_VELOCITY = -0.0784000015258789;

    public GroundSpoofB(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);
            double velocity = player.getVelocity().getY();

            // Checks the player for exemptions.
            if (playerData.getTicksSince(ActionType.RESPAWN) < 20
                    || playerData.getTicksSince(ActionType.TELEPORT) < 20
                    || playerData.getTicksSince(ActionType.LOGIN) < 20
                    || playerData.isNearClimbable()
                    || playerData.isAgainstBlock()
                    || playerData.isNearSlimeBlock()
                    || playerData.isNearGround()) {
                return;
            }

            // Checks for invalid on-ground Flying packets.
            if (!flying.isMoving() && !flying.isRotating() && flying.isOnGround()
                    && velocity != ON_GROUND_VELOCITY) {
                flag(true, "velocity=" + velocity
                        + " airTicks=" + playerData.getAirTicks()
                        + " airTicksStrict=" + playerData.getAirTicksStrict()
                );
            }
        }
    }
}
