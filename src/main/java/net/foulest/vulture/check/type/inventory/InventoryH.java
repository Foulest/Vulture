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
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "Inventory (H)", type = CheckType.INVENTORY, punishable = false)
public class InventoryH extends Check {

    private boolean pressed;
    private int lastSlot;
    private int lastButton;

    public InventoryH(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        // Checks the player for exemptions.
        if (playerData.getVersion().isNewerThan(ClientVersion.V_1_8)) {
            return;
        }

        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.CLICK_WINDOW) {
            @NotNull WrapperPlayClientClickWindow windowClick = new WrapperPlayClientClickWindow(event);
            WrapperPlayClientClickWindow.WindowClickType clickType = windowClick.getWindowClickType();
            int windowSlot = windowClick.getSlot();
            int windowButton = windowClick.getButton();
            int windowId = windowClick.getWindowId();

            if (windowId != 0 && clickType == WrapperPlayClientClickWindow.WindowClickType.SWAP) {
                if (pressed && lastSlot != windowSlot && lastButton == windowButton) {
                    KickUtil.kickPlayer(player, event, "Inventory (H) |"
                            + " (windowSlot=" + windowSlot
                            + " lastSlot=" + lastSlot
                            + " windowButton=" + windowButton
                            + " lastButton=" + lastButton + ")");
                }

                pressed = true;
            }

            lastSlot = windowSlot;
            lastButton = windowButton;

        } else if (packetType == PacketType.Play.Client.PLAYER_FLYING
                || packetType == PacketType.Play.Client.PLAYER_POSITION
                || packetType == PacketType.Play.Client.PLAYER_ROTATION
                || packetType == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            pressed = false;
        }
    }
}
