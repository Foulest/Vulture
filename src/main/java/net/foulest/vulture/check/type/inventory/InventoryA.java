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
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "Inventory (A)", type = CheckType.INVENTORY, punishable = false,
        description = "Detects this Inventory pattern: ArmAnimation, WindowClick, UseEntity, WindowClick")
public class InventoryA extends Check {

    private int stage;

    public InventoryA(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        // Checks the player for exemptions.
        if (!playerData.getVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
            return;
        }

        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.ANIMATION) {
            if (stage == 0) {
                ++stage;
            }

        } else if (packetType == PacketType.Play.Client.CLICK_WINDOW) {
            @NotNull WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
            WrapperPlayClientClickWindow.WindowClickType windowClickType = packet.getWindowClickType();
            int windowId = packet.getWindowId();

            if (windowClickType == WrapperPlayClientClickWindow.WindowClickType.SWAP
                    && windowId == 0 && (stage == 1 || stage == 3)) {
                ++stage;
            }

        } else if (packetType == PacketType.Play.Client.INTERACT_ENTITY) {
            @NotNull WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            WrapperPlayClientInteractEntity.InteractAction action = packet.getAction();

            if (action == WrapperPlayClientInteractEntity.InteractAction.ATTACK && stage == 2) {
                ++stage;
            }

        } else if (packetType == PacketType.Play.Client.PLAYER_FLYING
                || packetType == PacketType.Play.Client.PLAYER_POSITION
                || packetType == PacketType.Play.Client.PLAYER_ROTATION
                || packetType == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            @NotNull WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);

            if (packet.hasPositionChanged()) {
                stage = 0;
            }
        }

        if (stage == 4) {
            stage = 0;
            KickUtil.kickPlayer(player, event, "Inventory (A) | AutoHeal");
        }
    }
}
