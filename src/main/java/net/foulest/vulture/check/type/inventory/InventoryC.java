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
import io.github.retrooper.packetevents.packetwrappers.play.in.blockplace.WrappedPacketInBlockPlace;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.player.Direction;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import org.bukkit.Material;

@CheckInfo(name = "Inventory (C)", type = CheckType.INVENTORY,
        description = "Detects this Inventory pattern: HeldItemSlot, BlockPlace, HeldItemSlot")
public class InventoryC extends Check {

    private long start;
    private int stage;

    public InventoryC(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (packetId == PacketType.Play.Client.HELD_ITEM_SLOT) {
            if (stage == 0 || stage == 2) {
                if (stage == 0) {
                    start = System.currentTimeMillis();
                }

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
                        && stage == 1) {
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

        if (stage == 3 && timeDiff < 99) {
            stage = 0;
            flag(false, "timeDiff=" + timeDiff);
        }
    }
}
