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
import net.foulest.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import net.foulest.packetevents.packetwrappers.play.in.useentity.WrappedPacketInUseEntity;
import net.foulest.packetevents.packetwrappers.play.in.windowclick.WrappedPacketInWindowClick;
import net.foulest.packetevents.utils.player.ClientVersion;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;

@CheckInfo(name = "Inventory (A)", type = CheckType.INVENTORY, punishable = false,
        description = "Detects this Inventory pattern: ArmAnimation, WindowClick, UseEntity, WindowClick")
public class InventoryA extends Check {

    private int stage;

    public InventoryA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        // Checks the player for exemptions.
        if (!playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8_9)) {
            return;
        }

        if (packetId == PacketType.Play.Client.ARM_ANIMATION) {
            if (stage == 0) {
                ++stage;
            }

        } else if (packetId == PacketType.Play.Client.WINDOW_CLICK) {
            WrappedPacketInWindowClick windowClick = new WrappedPacketInWindowClick(nmsPacket);
            int windowId = windowClick.getWindowId();
            int windowMode = windowClick.getMode();

            if (windowId == 0 && windowMode == 2 && (stage == 1 || stage == 3)) {
                ++stage;
            }

        } else if (packetId == PacketType.Play.Client.USE_ENTITY) {
            WrappedPacketInUseEntity useEntity = new WrappedPacketInUseEntity(nmsPacket);
            WrappedPacketInUseEntity.EntityUseAction action = useEntity.getAction();

            if (action == WrappedPacketInUseEntity.EntityUseAction.ATTACK && stage == 2) {
                ++stage;
            }

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);

            if (flying.isMoving()) {
                stage = 0;
            }
        }

        if (stage == 4) {
            stage = 0;
            KickUtil.kickPlayer(player, event, "Inventory (A) | AutoHeal");
        }
    }
}
