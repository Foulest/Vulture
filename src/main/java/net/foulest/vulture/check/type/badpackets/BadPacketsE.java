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
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "BadPackets (E)", type = CheckType.BADPACKETS, punishable = false,
        description = "Detects sending invalid packets while in a bed.")
public class BadPacketsE extends Check {

    public BadPacketsE(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        int sleepTicks = player.getSleepTicks();

        // Checks the player for exemptions.
        if (sleepTicks < 10) {
            return;
        }

        PacketTypeCommon packetType = event.getPacketType();
        String packetName = packetType.getName();

        if (packetType == PacketType.Play.Client.PLAYER_FLYING
                || packetType == PacketType.Play.Client.PLAYER_POSITION
                || packetType == PacketType.Play.Client.PLAYER_ROTATION
                || packetType == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            @NotNull WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);
            Location location = flying.getLocation();
            Vector3d position = location.getPosition();

            if (flying.hasRotationChanged()) {
                KickUtil.kickPlayer(player, event, "BadPackets (E) | Sent invalid Rotation packet while in bed"
                        + " (ticks=" + sleepTicks + ")");
                return;
            }

            if (flying.hasPositionChanged()
                    && playerData.isMoving()
                    && !playerData.isTeleporting(position)) {
                KickUtil.kickPlayer(player, event, "BadPackets (E) | Sent invalid Position packet while in bed"
                        + " (ticks=" + sleepTicks + ")");
            }
        } else if (packetType != PacketType.Play.Client.CHAT_MESSAGE
                && packetType != PacketType.Play.Client.KEEP_ALIVE) {
            KickUtil.kickPlayer(player, event, "BadPackets (E) | Sent invalid packet while in bed"
                    + " (packetName=" + packetName + " ticks=" + sleepTicks + ")");
        }
    }
}
