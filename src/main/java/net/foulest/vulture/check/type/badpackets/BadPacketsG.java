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
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "BadPackets (G)", type = CheckType.BADPACKETS, punishable = false,
        description = "Detects breaking blocks too quickly.")
public class BadPacketsG extends Check {

    private int ticks;
    private int stage;

    public BadPacketsG(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.PLAYER_FLYING
                || packetType == PacketType.Play.Client.PLAYER_POSITION
                || packetType == PacketType.Play.Client.PLAYER_ROTATION
                || packetType == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (stage == 1) {
                ++ticks;
                stage = 2;
            } else {
                stage = 0;
            }

        } else if (packetType == PacketType.Play.Client.PLAYER_DIGGING) {
            @NotNull WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
            DiggingAction digType = packet.getAction();

            if (digType == DiggingAction.FINISHED_DIGGING) {
                stage = 1;

            } else if (digType == DiggingAction.START_DIGGING) {
                if (stage == 2 && (ticks != 1 || playerData.getVersion().isOlderThanOrEquals(ClientVersion.V_1_8))) {
                    KickUtil.kickPlayer(player, event, "BadPackets (G) |"
                            + " (ticks=" + ticks + ")");
                }

                stage = 0;
                ticks = 0;
            }
        }
    }
}
