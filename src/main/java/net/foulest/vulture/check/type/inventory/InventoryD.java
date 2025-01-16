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
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "Inventory (D)", type = CheckType.INVENTORY, punishable = false,
        description = "Detects sending invalid ReleaseUseItem packets.")
public class InventoryD extends Check {

    private boolean sent;

    public InventoryD(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        // Checks the player for exemptions.
        if (playerData.getVersion().isNewerThan(ClientVersion.V_1_8)) {
            return;
        }

        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            @NotNull WrapperPlayClientPlayerBlockPlacement blockPlace = new WrapperPlayClientPlayerBlockPlacement(event);
            BlockFace blockFace = blockPlace.getFace();

            if (blockFace == BlockFace.OTHER) {
                sent = true;
            }

        } else if (packetType == PacketType.Play.Client.PLAYER_DIGGING) {
            @NotNull WrapperPlayClientPlayerDigging blockDig = new WrapperPlayClientPlayerDigging(event);
            DiggingAction digType = blockDig.getAction();

            if (digType == DiggingAction.RELEASE_USE_ITEM && !sent) {
                KickUtil.kickPlayer(player, event, "Inventory (D) | ReleaseUseItem");
            }

            sent = false;
        }
    }
}
