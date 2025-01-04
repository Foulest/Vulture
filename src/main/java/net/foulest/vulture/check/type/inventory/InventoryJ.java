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
import net.foulest.packetevents.packetwrappers.play.in.blockplace.WrappedPacketInBlockPlace;
import net.foulest.packetevents.utils.player.Direction;
import net.foulest.packetevents.utils.vector.Vector3i;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;

@CheckInfo(name = "Inventory (J)", type = CheckType.INVENTORY, punishable = false,
        description = "Checks for invalid yaw differences.")
public class InventoryJ extends Check {

    private double lastX;
    private double lastY;
    private double lastZ;
    private Vector3i lastBlockPosition;
    private Float lastYaw;

    public InventoryJ(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (packetId == PacketType.Play.Client.BLOCK_PLACE) {
            WrappedPacketInBlockPlace blockPlace = new WrappedPacketInBlockPlace(nmsPacket);

            if (blockPlace.getDirection() != Direction.OTHER) {
                Vector3i blockPosition = blockPlace.getBlockPosition();
                int blockX = blockPosition.getX();
                int blockY = blockPosition.getY();
                int blockZ = blockPosition.getZ();

                if (lastBlockPosition != null && blockX == lastX && blockY == lastY && blockZ == lastZ) {
                    int lastBlockX = lastBlockPosition.getX();
                    int lastBlockZ = lastBlockPosition.getZ();

                    if (Math.abs(blockX - lastBlockX) + Math.abs(blockZ - lastBlockZ) == 1) {
                        float yaw = playerData.getPlayer().getLocation().getYaw();

                        if (lastYaw != null) {
                            double yawDiff = Math.abs(yaw - lastYaw);

                            if (yawDiff > 20.0) {
                                KickUtil.kickPlayer(player, event, "Inventory (J) | Invalid yaw difference |"
                                        + " (yawDiff=" + yawDiff + ")"
                                );
                            }
                        }

                        lastYaw = yaw;
                    }
                }

                lastX = blockX;
                lastY = blockY;
                lastZ = blockZ;
                lastBlockPosition = blockPosition;
            }
        }
    }
}
