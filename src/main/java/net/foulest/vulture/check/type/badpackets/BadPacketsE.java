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
package net.foulest.vulture.check.type.badpackets;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;

@CheckInfo(name = "BadPackets (E)", type = CheckType.BADPACKETS, punishable = false,
        description = "Detects sending invalid packets while in a bed.")
public class BadPacketsE extends Check {

    public BadPacketsE(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        int sleepTicks = player.getSleepTicks();

        // Checks the player for exemptions.
        if (sleepTicks < 10) {
            return;
        }

        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);
            Vector3d flyingPosition = flying.getPosition();

            if (flying.isRotating()) {
                KickUtil.kickPlayer(player, event, "BadPackets (E) | Sent invalid Rotation packet while in bed"
                        + " (ticks=" + sleepTicks + ")");
            }

            if (flying.isMoving() && playerData.isMoving() && !playerData.isTeleporting(flyingPosition)) {
                KickUtil.kickPlayer(player, event, "BadPackets (E) | Sent invalid Position packet while in bed"
                        + " (ticks=" + sleepTicks + ")");
            }
        } else if (packetId != PacketType.Play.Client.CHAT
                && packetId != PacketType.Play.Client.KEEP_ALIVE) {
            String packetName = PacketType.getPacketFromId(packetId).getSimpleName();
            KickUtil.kickPlayer(player, event, "BadPackets (E) | Sent invalid packet while in bed"
                    + " (packetName=" + packetName + " ticks=" + sleepTicks + ")");
        }
    }
}
