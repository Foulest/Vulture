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
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "Inventory (J)", type = CheckType.INVENTORY, punishable = false,
        description = "Checks for invalid yaw differences.")
public class InventoryJ extends Check {

    private double lastX;
    private double lastY;
    private double lastZ;
    private Vector3i lastBlockPosition;
    private Float lastYaw;

    public InventoryJ(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            @NotNull WrapperPlayClientPlayerBlockPlacement blockPlace = new WrapperPlayClientPlayerBlockPlacement(event);
            BlockFace blockFace = blockPlace.getFace();

            if (blockFace == BlockFace.OTHER) {
                return;
            }

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
                            KickUtil.kickPlayer(player, event, "Inventory (J) |"
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
