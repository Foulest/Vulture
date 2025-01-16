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
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "Inventory (G)", type = CheckType.INVENTORY, punishable = false)
public class InventoryG extends Check {

    private boolean wasOpen;
    private boolean open;

    public InventoryG(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        // Checks the player for exemptions.
        if (playerData.getVersion().isNewerThan(ClientVersion.V_1_8)) {
            return;
        }

        if (packetType == PacketType.Play.Client.CLOSE_WINDOW) {
            if (wasOpen) {
                KickUtil.kickPlayer(player, event, "Inventory (G) | Opening inventory twice");
            }

        } else if (packetType == PacketType.Play.Client.CLIENT_STATUS) {
            @NotNull WrapperPlayClientClientStatus clientStatus = new WrapperPlayClientClientStatus(event);
            WrapperPlayClientClientStatus.Action action = clientStatus.getAction();

            if (action == WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT) {
                open = true;
            }

        } else if (packetType == PacketType.Play.Client.CLICK_WINDOW) {
            wasOpen = open;

            if (open) {
                open = false;
            }

        } else if (packetType == PacketType.Play.Client.PLAYER_FLYING
                || packetType == PacketType.Play.Client.PLAYER_POSITION
                || packetType == PacketType.Play.Client.PLAYER_ROTATION
                || packetType == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            open = false;
            wasOpen = false;
        }
    }
}
