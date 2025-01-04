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
import net.foulest.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import net.foulest.packetevents.packetwrappers.play.in.windowclick.WrappedPacketInWindowClick;
import net.foulest.packetevents.utils.player.Direction;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.bukkit.Material;

@CheckInfo(name = "Inventory (B)", type = CheckType.INVENTORY, punishable = false,
        description = "Detects this Inventory pattern: ClickWindow, HeldItemSlot, BlockPlace, HeldItemSlot")
public class InventoryB extends Check {

    private long start;
    private int stage;

    public InventoryB(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (packetId == PacketType.Play.Client.WINDOW_CLICK) {
            WrappedPacketInWindowClick windowClick = new WrappedPacketInWindowClick(nmsPacket);
            int windowId = windowClick.getWindowId();

            if (windowId == 0 && stage == 0) {
                ++stage;
                start = System.currentTimeMillis();
            }

        } else if (packetId == PacketType.Play.Client.HELD_ITEM_SLOT) {
            if (stage == 1 || stage == 3) {
                ++stage;
            }

        } else if (packetId == PacketType.Play.Client.BLOCK_PLACE) {
            WrappedPacketInBlockPlace blockPlace = new WrappedPacketInBlockPlace(nmsPacket);

            blockPlace.getItemStack().ifPresent(itemStack -> {
                Material itemType = itemStack.getType();
                Direction direction = blockPlace.getDirection();

                if ((itemType == Material.MUSHROOM_SOUP
                        || itemType == Material.POTION
                        || itemType == Material.BOWL)
                        && direction == Direction.OTHER
                        && stage == 2) {
                    ++stage;
                }
            });

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);

            if (!flying.isRotating() && !flying.isMoving()) {
                stage = 0;
            }
        }

        long timeDiff = System.currentTimeMillis() - start;

        if (stage == 4 && timeDiff < 350) {
            stage = 0;
            KickUtil.kickPlayer(player, event, "Inventory (B) | AutoHeal | timeDiff=" + timeDiff);
        }
    }
}
