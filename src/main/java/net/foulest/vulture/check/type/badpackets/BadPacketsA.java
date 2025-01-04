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
package net.foulest.vulture.check.type.badpackets;

import net.foulest.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import net.foulest.packetevents.packettype.PacketType;
import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.play.in.setcreativeslot.WrappedPacketInSetCreativeSlot;
import net.foulest.packetevents.utils.player.ClientVersion;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

@CheckInfo(name = "BadPackets (A)", type = CheckType.BADPACKETS, punishable = false,
        description = "Detects sending too many packets in the same tick.")
public class BadPacketsA extends Check {

    public BadPacketsA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        synchronized (playerData.getPacketCounts()) {
            Map<Byte, Integer> packetCounts = playerData.getPacketCounts();
            packetCounts.compute(packetId, (key, count) -> (count == null) ? 1 : count + 1);
            Integer count = packetCounts.get(packetId);

            if (count == null) {
                // This normally shouldn't happen, but it's possible
                // if the map is cleared after compute but before get
                return;
            }

            String packetName = PacketType.getPacketFromId(packetId).getSimpleName();
            boolean olderThan1_8 = playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8_9);
            boolean inCreative = player.getGameMode() == GameMode.CREATIVE;

            switch (packetId) {
                case PacketType.Play.Client.BLOCK_PLACE:
                case PacketType.Play.Client.HELD_ITEM_SLOT: {
                    int threshold = olderThan1_8 && inCreative ? 3 : 5;

                    if (count >= threshold) {
                        KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                                + " packet=" + packetName
                                + " count=" + count);
                    }
                    break;
                }

                case PacketType.Play.Client.CLIENT_COMMAND:
                case PacketType.Play.Client.TAB_COMPLETE:
                    if (count >= 3) {
                        KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                                + " packet=" + packetName
                                + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.CUSTOM_PAYLOAD:
                    if (count >= 15) {
                        KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                                + " packet=" + packetName
                                + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.WINDOW_CLICK: {
                    int threshold = olderThan1_8 ? 52 : 6;

                    if (count >= threshold) {
                        KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                                + " packet=" + packetName
                                + " count=" + count);
                    }
                    break;
                }

                case PacketType.Play.Client.ENTITY_ACTION:
                    if (count >= 5) {
                        KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                                + " packet=" + packetName
                                + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.BLOCK_DIG:
                    if (count >= 6) {
                        KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                                + " packet=" + packetName
                                + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.USE_ENTITY:
                    if (count >= 10) {
                        KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                                + " packet=" + packetName
                                + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.ARM_ANIMATION:
                    if (count >= 9) {
                        KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                                + " packet=" + packetName
                                + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.SET_CREATIVE_SLOT:
                    WrappedPacketInSetCreativeSlot creativeSlot = new WrappedPacketInSetCreativeSlot(nmsPacket);
                    ItemStack clickedItem = creativeSlot.getClickedItem();
                    Material clickedItemType = clickedItem.getType();
                    String clickedItemName = clickedItemType.name();

                    if (clickedItemType == Material.AIR) {
                        if (count >= 50) {
                            KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                                    + " packet=" + packetName
                                    + " count=" + count);
                        }
                    } else {
                        if (count >= 30) {
                            int slot = creativeSlot.getSlot();

                            KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                                    + " packet=" + packetName
                                    + " count=" + count
                                    + " slot=" + slot
                                    + " item=" + clickedItemName);
                        }
                    }
                    break;

                case PacketType.Play.Client.SETTINGS:
                    if (count >= 41) {
                        KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                                + " packet=" + packetName
                                + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.STEER_VEHICLE:
                    int threshold;

                    if (playerData.getTicksSince(ActionType.LOGIN) < 40) {
                        threshold = 13;
                    } else {
                        threshold = olderThan1_8 ? 3 : 5;
                    }

                    if (count >= threshold) {
                        KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                                + " packet=" + packetName
                                + " count=" + count);
                    }
                    break;

                case PacketType.Play.Client.FLYING:
                case PacketType.Play.Client.LOOK:
                case PacketType.Play.Client.POSITION:
                case PacketType.Play.Client.POSITION_LOOK:
                case PacketType.Play.Client.KEEP_ALIVE:
                case PacketType.Play.Client.TRANSACTION:
                    break;

                default:
                    if (count >= 2) {
                        KickUtil.kickPlayer(player, event, "BadPackets (A) |"
                                + " packet=" + packetName
                                + " count=" + count);
                    }
                    break;
            }
        }
    }
}
