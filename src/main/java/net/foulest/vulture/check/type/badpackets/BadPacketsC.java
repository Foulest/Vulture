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
import io.github.retrooper.packetevents.packetwrappers.play.in.useentity.WrappedPacketInUseEntity;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.ToString;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;

@ToString
@CheckInfo(name = "BadPackets (C)", type = CheckType.BADPACKETS, punishable = false,
        description = "Detects sending attack packets without swinging.")
public class BadPacketsC extends Check {

    private boolean swung;

    public BadPacketsC(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        // Checks the player for exemptions.
        if (playerData.getVersion().isNewerThan(ClientVersion.v_1_8)) {
            return;
        }

        if (packetId == PacketType.Play.Client.USE_ENTITY) {
            WrappedPacketInUseEntity useEntity = new WrappedPacketInUseEntity(nmsPacket);
            WrappedPacketInUseEntity.EntityUseAction action = useEntity.getAction();

            if (action == WrappedPacketInUseEntity.EntityUseAction.ATTACK) {
                if (!swung) {
                    KickUtil.kickPlayer(player, event, "Attacked an entity without swinging");
                    return;
                }

                swung = false;
            }

        } else if (packetId == PacketType.Play.Client.ARM_ANIMATION) {
            swung = true;

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            swung = false;
        }
    }
}
