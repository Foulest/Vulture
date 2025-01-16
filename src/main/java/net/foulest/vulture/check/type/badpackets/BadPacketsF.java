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
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "BadPackets (F)", type = CheckType.BADPACKETS, punishable = false,
        description = "Detects sending invalid UpdateSign packets.")
public class BadPacketsF extends Check {

    private boolean sentUpdateSign;
    private boolean sentSignEditor;
    private boolean sentBlockChange;

    public BadPacketsF(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.UPDATE_SIGN) {
            if (!sentSignEditor) {
                KickUtil.kickPlayer(player, event, "BadPackets (F) | Sent UpdateSign packet without SignEditor");
            }

            sentUpdateSign = true;

        } else if (packetType == PacketType.Play.Client.PLAYER_FLYING
                || packetType == PacketType.Play.Client.PLAYER_POSITION
                || packetType == PacketType.Play.Client.PLAYER_ROTATION
                || packetType == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (sentUpdateSign && !sentBlockChange) {
                KickUtil.kickPlayer(player, event, "BadPackets (F) | Sent UpdateSign packet without BlockChange");
            }

            sentUpdateSign = false;
            sentSignEditor = false;
            sentBlockChange = false;
        }
    }

    @Override
    public void handle(@NotNull PacketPlaySendEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Server.OPEN_SIGN_EDITOR) {
            sentSignEditor = true;
            sentBlockChange = false;

        } else if (packetType == PacketType.Play.Server.BLOCK_CHANGE) {
            sentBlockChange = true;
            sentSignEditor = false;
        }
    }
}
