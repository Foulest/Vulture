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
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.ToString;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

@ToString
@CheckInfo(name = "BadPackets (B)", type = CheckType.BADPACKETS, punishable = false,
        description = "Detects ignoring the mandatory Position packet.")
public class BadPacketsB extends Check {

    private long lastPosition;
    private long lastTransaction;

    public BadPacketsB(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
        lastPosition = System.currentTimeMillis();
        lastTransaction = System.currentTimeMillis();
    }

    @Override
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        // Ignores players in Spectator mode, as they don't send Position packets.
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.PLAYER_FLYING
                || packetType == PacketType.Play.Client.PLAYER_POSITION
                || packetType == PacketType.Play.Client.PLAYER_ROTATION
                || packetType == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            @NotNull WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);

            if (flying.hasPositionChanged()) {
                lastPosition = System.currentTimeMillis();
            }

        } else if (packetType == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            lastTransaction = System.currentTimeMillis();

        } else if (packetType == PacketType.Play.Client.KEEP_ALIVE) {
            // Checks the player for exemptions.
            if (player.isDead()
                    || playerData.getTicksSince(ActionType.STEER_VEHICLE) < 100
                    || playerData.getTicksSince(ActionType.LOGIN) < 200) {
                return;
            }

            long timeSincePosition = System.currentTimeMillis() - lastPosition;
            long timeSinceTransaction = System.currentTimeMillis() - lastTransaction;

            // If the player hasn't sent a Position packet in the last 5 seconds, kick them.
            if (timeSincePosition > 5000 && timeSinceTransaction < 1000) {
                KickUtil.kickPlayer(player, "BadPackets (B) | Might be cancelling sending Position packets");
            }
        }
    }
}
