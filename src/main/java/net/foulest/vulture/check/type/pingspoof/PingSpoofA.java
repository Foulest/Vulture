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
package net.foulest.vulture.check.type.pingspoof;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.data.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractQueue;
import java.util.Comparator;
import java.util.PriorityQueue;

@CheckInfo(name = "PingSpoof (A)", type = CheckType.PINGSPOOF, punishable = false,
        description = "Detects clients modifying Transaction packets.")
public class PingSpoofA extends Check {

    private final AbstractQueue<Pair<Short, Long>> transactionsOut = new PriorityQueue<>(100, Comparator.comparingLong(Pair::getLast));

    private int transactionsInCount;
    private int transactionsOutCount;

    public PingSpoofA(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            @NotNull WrapperPlayClientWindowConfirmation transaction = new WrapperPlayClientWindowConfirmation(event);
            short actionId = transaction.getActionId();

            // Increments the count of Transaction packets received.
            transactionsInCount++;

            // If the client has sent more Transaction packets than received, kick them.
            if (transactionsInCount - transactionsOutCount > 1) {
                KickUtil.kickPlayer(player, event, "Sent more Transaction packets than received");
                return;
            }

            // Remove the Transaction packet sent by the server.
            if (transactionsOut.stream().map(Pair::getFirst).anyMatch(first -> first == actionId)) {
                transactionsOut.removeIf(pair -> {
                    @NotNull Short first = pair.getFirst();
                    return first == actionId;
                });
            }
        }
    }

    @Override
    public void handle(@NotNull PacketPlaySendEvent event) {
        PacketTypeCommon packetType = event.getPacketType();
        long timestamp = event.getTimestamp();

        if (packetType == PacketType.Play.Server.WINDOW_CONFIRMATION) {
            @NotNull WrapperPlayServerWindowConfirmation transaction = new WrapperPlayServerWindowConfirmation(event);
            short actionId = transaction.getActionId();

            // Adds the Transaction packet sent by the server.
            transactionsOut.add(new Pair<>(actionId, timestamp));
            transactionsOutCount++;
        }
    }
}
