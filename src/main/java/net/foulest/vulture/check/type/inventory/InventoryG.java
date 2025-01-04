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

import net.foulest.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import net.foulest.packetevents.packettype.PacketType;
import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.play.in.clientcommand.WrappedPacketInClientCommand;
import net.foulest.packetevents.utils.player.ClientVersion;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;

@CheckInfo(name = "Inventory (G)", type = CheckType.INVENTORY, punishable = false)
public class InventoryG extends Check {

    private boolean wasOpen;
    private boolean open;

    public InventoryG(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        // Checks the player for exemptions.
        if (playerData.getVersion().isNewerThan(ClientVersion.v_1_8_9)) {
            return;
        }

        if (packetId == PacketType.Play.Client.CLOSE_WINDOW) {
            if (wasOpen) {
                KickUtil.kickPlayer(player, event, "Inventory (G) | Opening inventory twice");
            }

        } else if (packetId == PacketType.Play.Client.CLIENT_COMMAND) {
            WrappedPacketInClientCommand clientCommand = new WrappedPacketInClientCommand(nmsPacket);
            WrappedPacketInClientCommand.ClientCommand command = clientCommand.getClientCommand();

            if (command == WrappedPacketInClientCommand.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                open = true;
            }

        } else if (packetId == PacketType.Play.Client.WINDOW_CLICK) {
            wasOpen = open;

            if (open) {
                open = false;
            }

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            open = false;
            wasOpen = false;
        }
    }
}
