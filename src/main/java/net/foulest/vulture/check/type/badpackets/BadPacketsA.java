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
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@CheckInfo(name = "BadPackets (A)", type = CheckType.BADPACKETS, punishable = false,
        description = "Detects sending too many packets in the same tick.")
public class BadPacketsA extends Check {

    public BadPacketsA(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        synchronized (playerData.getPacketCounts()) {
            Map<Integer, Integer> packetCounts = playerData.getPacketCounts();
            int packetId = event.getPacketId();
            packetCounts.compute(packetId, (key, count) -> (count == null) ? 1 : count + 1);
            Integer count = packetCounts.get(packetId);

            if (count == null) {
                // This normally shouldn't happen, but it's possible
                // if the map is cleared after compute but before get
                return;
            }

            boolean olderThan1_8 = playerData.getVersion().isOlderThanOrEquals(ClientVersion.V_1_8);
            boolean inCreative = player.getGameMode() == GameMode.CREATIVE;

            PacketTypeCommon packetType = event.getPacketType();
            String packetName = packetType.getName();

            if (packetType.equals(PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT)
                    || packetType.equals(PacketType.Play.Client.HELD_ITEM_CHANGE)) {
                int threshold = olderThan1_8 && inCreative ? 3 : 5;

                if (count >= threshold) {
                    KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                            + " packet=" + packetName
                            + " count=" + count);
                }
                return;
            }

            if (packetType.equals(PacketType.Play.Client.CLIENT_STATUS)
                    || packetType.equals(PacketType.Play.Client.TAB_COMPLETE)) {
                if (count >= 3) {
                    KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                            + " packet=" + packetName
                            + " count=" + count);
                }
                return;
            }

            if (packetType.equals(PacketType.Play.Client.PLUGIN_MESSAGE)) {
                if (count >= 15) {
                    KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                            + " packet=" + packetName
                            + " count=" + count);
                }
                return;
            }

            if (packetType.equals(PacketType.Play.Client.CLICK_WINDOW)) {
                int threshold = olderThan1_8 ? 52 : 6;

                if (count >= threshold) {
                    KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                            + " packet=" + packetName
                            + " count=" + count);
                }
                return;
            }

            if (packetType.equals(PacketType.Play.Client.ENTITY_ACTION)) {
                if (count >= 5) {
                    KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                            + " packet=" + packetName
                            + " count=" + count);
                }
                return;
            }

            if (packetType.equals(PacketType.Play.Client.PLAYER_DIGGING)) {
                if (count >= 6) {
                    KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                            + " packet=" + packetName
                            + " count=" + count);
                }
                return;
            }

            if (packetType.equals(PacketType.Play.Client.INTERACT_ENTITY)) {
                if (count >= 10) {
                    KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                            + " packet=" + packetName
                            + " count=" + count);
                }
                return;
            }

            if (packetType.equals(PacketType.Play.Client.ANIMATION)) {
                if (count >= 9) {
                    KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                            + " packet=" + packetName
                            + " count=" + count);
                }
                return;
            }

            if (packetType.equals(PacketType.Play.Client.CREATIVE_INVENTORY_ACTION)
                    || packetType.equals(PacketType.Play.Client.CLIENT_SETTINGS)) {
                if (count >= 50) {
                    KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                            + " packet=" + packetName
                            + " count=" + count);
                }
                return;
            }

            if (packetType.equals(PacketType.Play.Client.STEER_VEHICLE)) {
                int threshold;

                if (playerData.getTicksSince(ActionType.LOGIN) < 40) {
                    threshold = 13;
                } else {
                    threshold = olderThan1_8 ? 4 : 5;
                }

                if (count >= threshold) {
                    KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                            + " packet=" + packetName
                            + " count=" + count);
                }
                return;
            }

            if (!packetType.equals(PacketType.Play.Client.PLAYER_FLYING)
                    && !packetType.equals(PacketType.Play.Client.PLAYER_ROTATION)
                    && !packetType.equals(PacketType.Play.Client.PLAYER_POSITION)
                    && !packetType.equals(PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION)
                    && !packetType.equals(PacketType.Play.Client.KEEP_ALIVE)
                    && !packetType.equals(PacketType.Play.Client.WINDOW_CONFIRMATION)) {
                if (count >= 2) {
                    KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                            + " packet=" + packetName
                            + " count=" + count);
                }
            }
        }
    }
}
