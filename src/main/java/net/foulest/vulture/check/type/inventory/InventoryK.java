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
package net.foulest.vulture.check.type.inventory;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "Inventory (K)", type = CheckType.INVENTORY, punishable = false,
        description = "Detects sending multiple ReleaseUseItem packets in the same tick.")
public class InventoryK extends Check {

    private int buffer;

    public InventoryK(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.PLAYER_DIGGING) {
            @NotNull WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
            DiggingAction digType = wrapper.getAction();

            if (digType == DiggingAction.RELEASE_USE_ITEM) {
                ++buffer;

                if (buffer > 2) {
                    KickUtil.kickPlayer(player, event, "Inventory (K) | Multiple ReleaseUseItem packets");
                }
            }
        } else if (packetType == PacketType.Play.Client.PLAYER_FLYING
                || packetType == PacketType.Play.Client.PLAYER_POSITION
                || packetType == PacketType.Play.Client.PLAYER_ROTATION
                || packetType == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            buffer = 0;
        }
    }
}
