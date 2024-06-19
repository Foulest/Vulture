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
import io.github.retrooper.packetevents.packetwrappers.play.in.resourcepackstatus.WrappedPacketInResourcePackStatus;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;

@CheckInfo(name = "BadPackets (E)", type = CheckType.BADPACKETS,
        acceptsServerPackets = true, punishable = false,
        description = "Detects sending invalid ResourcePackStatus packets.")
public class BadPacketsE extends Check {

    private boolean accepted;
    private int packetsSent;
    private int packetsReceived;

    public BadPacketsE(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (packetId == PacketType.Play.Server.RESOURCE_PACK_SEND) {
            ++packetsSent;

        } else if (packetId == PacketType.Play.Client.RESOURCE_PACK_STATUS) {
            WrappedPacketInResourcePackStatus resourcePackStatus = new WrappedPacketInResourcePackStatus(nmsPacket);
            WrappedPacketInResourcePackStatus.ResourcePackStatus status = resourcePackStatus.getStatus();

            // Keeps track of packets received.
            if (status != WrappedPacketInResourcePackStatus.ResourcePackStatus.ACCEPTED) {
                ++packetsReceived;
            }

            // Detects receiving more packets than sent.
            if (packetsReceived > packetsSent) {
                KickUtil.kickPlayer(player, event, "Sent more ResourcePackStatus packets than received");
                return;
            }

            // Detects sending two ACCEPTED packets in a row.
            if (status == WrappedPacketInResourcePackStatus.ResourcePackStatus.ACCEPTED) {
                if (accepted) {
                    KickUtil.kickPlayer(player, event, "Sent two ResourcePackStatus ACCEPTED packets in a row");
                    return;
                }
                accepted = true;
            } else {
                accepted = false;
            }
        }
    }
}
