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
package net.foulest.vulture.check.type.badpackets;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientResourcePackStatus;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "BadPackets (D)", type = CheckType.BADPACKETS, punishable = false,
        description = "Detects sending invalid ResourcePackStatus packets.")
public class BadPacketsD extends Check {

    private boolean accepted;
    private int packetsSent;
    private int packetsReceived;

    public BadPacketsD(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.RESOURCE_PACK_STATUS) {
            @NotNull WrapperPlayClientResourcePackStatus packet = new WrapperPlayClientResourcePackStatus(event);
            WrapperPlayClientResourcePackStatus.Result result = packet.getResult();

            // Keeps track of packets received.
            if (result != WrapperPlayClientResourcePackStatus.Result.ACCEPTED) {
                ++packetsReceived;
            }

            // Detects receiving more packets than sent.
            if (packetsReceived > packetsSent) {
                KickUtil.kickPlayer(player, event, "BadPackets (D) | Sent more ResourcePackStatus packets than received");
                return;
            }

            // Detects sending two ACCEPTED packets in a row.
            if (result == WrapperPlayClientResourcePackStatus.Result.ACCEPTED) {
                if (accepted) {
                    KickUtil.kickPlayer(player, event, "BadPackets (D) | Sent two ResourcePackStatus ACCEPTED packets in a row");
                    return;
                }

                accepted = true;
            } else {
                accepted = false;
            }
        }
    }

    @Override
    public void handle(@NotNull PacketPlaySendEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Server.RESOURCE_PACK_SEND) {
            ++packetsSent;
        }
    }
}
