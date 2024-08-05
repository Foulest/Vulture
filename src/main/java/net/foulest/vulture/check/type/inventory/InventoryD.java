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
package net.foulest.vulture.check.type.inventory;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.clientcommand.WrappedPacketInClientCommand;
import io.github.retrooper.packetevents.packetwrappers.play.in.closewindow.WrappedPacketInCloseWindow;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.packetwrappers.play.in.windowclick.WrappedPacketInWindowClick;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.ToString;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@ToString
@CheckInfo(name = "Inventory (D)", type = CheckType.INVENTORY,
        description = "Detects this Inventory pattern: OpenInventory, WindowClick (Quick Move/Throw), CloseWindow")
public class InventoryD extends Check {

    private int stage;

    public InventoryD(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        // Checks the player for exemptions.
        if (!playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8)) {
            return;
        }

        if (packetId == PacketType.Play.Client.CLIENT_COMMAND) {
            WrappedPacketInClientCommand clientCommand = new WrappedPacketInClientCommand(nmsPacket);
            WrappedPacketInClientCommand.ClientCommand command = clientCommand.getClientCommand();

            if (command == WrappedPacketInClientCommand.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT && stage == 0) {
                ++stage;
            }

        } else if (packetId == PacketType.Play.Client.WINDOW_CLICK) {
            WrappedPacketInWindowClick windowClick = new WrappedPacketInWindowClick(nmsPacket);
            int windowId = windowClick.getWindowId();
            int windowMode = windowClick.getMode();

            if (windowId == 0 && (windowMode == 1 || windowMode == 4) && stage == 1) {
                ++stage;
            }

        } else if (packetId == PacketType.Play.Client.CLOSE_WINDOW) {
            WrappedPacketInCloseWindow closeWindow = new WrappedPacketInCloseWindow(nmsPacket);
            int windowId = closeWindow.getWindowId();

            if (windowId == 0 && stage == 2) {
                ++stage;
            }

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);

            if (flying.isMoving()) {
                stage = 0;
            }
        }

        if (stage == 3) {
            stage = 0;
            flag(false);
        }
    }
}
