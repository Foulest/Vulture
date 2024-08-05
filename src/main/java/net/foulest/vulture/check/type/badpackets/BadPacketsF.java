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
import lombok.ToString;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@ToString
@CheckInfo(name = "BadPackets (F)", type = CheckType.BADPACKETS, acceptsServerPackets = true,
        description = "Detects sending invalid UpdateSign packets.")
public class BadPacketsF extends Check {

    private boolean sentUpdateSign;
    private boolean sentSignEditor;
    private boolean sentBlockChange;

    public BadPacketsF(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (packetId == PacketType.Play.Server.OPEN_SIGN_EDITOR) {
            sentSignEditor = true;
            sentBlockChange = false;

        } else if (packetId == PacketType.Play.Server.BLOCK_CHANGE) {
            sentBlockChange = true;
            sentSignEditor = false;

        } else if (packetId == PacketType.Play.Client.UPDATE_SIGN) {
            if (!sentSignEditor) {
                flag(false, event, "Sent UpdateSign packet without SignEditor");
            }

            sentUpdateSign = true;

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            if (sentUpdateSign && !sentBlockChange) {
                flag(false, "Sent UpdateSign packet without BlockChange");
            }

            sentUpdateSign = false;
            sentSignEditor = false;
            sentBlockChange = false;
        }
    }
}
