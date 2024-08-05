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
package net.foulest.vulture.check.type.autoblock;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.blockdig.WrappedPacketInBlockDig;
import io.github.retrooper.packetevents.packetwrappers.play.in.blockplace.WrappedPacketInBlockPlace;
import io.github.retrooper.packetevents.utils.player.Direction;
import lombok.ToString;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@ToString
@CheckInfo(name = "AutoBlock (D)", type = CheckType.AUTOBLOCK,
        description = "Detects sending invalid ReleaseUseItem packets.")
public class AutoBlockD extends Check {

    private boolean sent;

    public AutoBlockD(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (packetId == PacketType.Play.Client.BLOCK_PLACE) {
            WrappedPacketInBlockPlace blockPlace = new WrappedPacketInBlockPlace(nmsPacket);
            Direction direction = blockPlace.getDirection();

            if (direction == Direction.OTHER) {
                sent = true;
            }

        } else if (packetId == PacketType.Play.Client.BLOCK_DIG) {
            WrappedPacketInBlockDig blockDig = new WrappedPacketInBlockDig(nmsPacket);
            WrappedPacketInBlockDig.PlayerDigType digType = blockDig.getDigType();

            if (digType == WrappedPacketInBlockDig.PlayerDigType.RELEASE_USE_ITEM && !sent) {
                flag(false);
            }

            sent = false;
        }
    }
}
