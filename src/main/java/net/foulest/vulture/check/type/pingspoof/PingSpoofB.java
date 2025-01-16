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
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.data.EvictingList;
import net.foulest.vulture.util.data.Pair;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "PingSpoof (B)", type = CheckType.PINGSPOOF, punishable = false,
        description = "Detects clients modifying KeepAlive packets.")
public class PingSpoofB extends Check {

    private final EvictingList<Pair<Long, Long>> keepAliveOut = new EvictingList<>(10);
    private final EvictingList<Long> pingValues = new EvictingList<>(5);

    private int keepAliveInCount;
    private int keepAliveOutCount;
    private int negativeStreak;

    public static long maxPing;
    public static long maxAveragePing;
    public static long maxPingDeviation;

    public PingSpoofB(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    @SuppressWarnings("NestedMethodCall")
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        long timeSinceRespawn = playerData.getTicksSince(ActionType.RESPAWN);
        long timeSinceTeleport = playerData.getTicksSince(ActionType.TELEPORT);
        long timeSinceLogin = playerData.getTicksSince(ActionType.LOGIN);

        if (packetType == PacketType.Play.Client.KEEP_ALIVE) {
            @NotNull WrapperPlayClientKeepAlive keepAlive = new WrapperPlayClientKeepAlive(event);
            long keepAliveId = keepAlive.getId();
            long timestamp = System.currentTimeMillis();

            // Increments the count of KeepAlive packets received.
            keepAliveInCount++;

            // Ignores the first KeepAlive packet received.
            if (keepAliveOut.isEmpty() || keepAliveOut.size() == 1) {
                return;
            }

            // Calculates the ping and adds it to the list.
            long ping = timestamp - keepAliveOut.getLast().getLast();
            pingValues.add(ping);

            // Calculates the average ping and ping deviation.
            int averagePing = (int) pingValues.stream().mapToLong(val -> val).average().orElse(0.0);
            int pingDeviation = (int) Math.sqrt(pingValues.stream().mapToLong(val -> val).map(i -> i - averagePing).map(i -> i * i).average().orElse(0.0));

            // If the client's ping is too high, kick them.
            if (timeSinceLogin > 20000L) {
                if (ping >= maxPing) {
                    KickUtil.kickPlayer(player, event, "Player's current ping exceeds the limits (Ping: " + ping + "ms)");
                    return;
                }

                // If the client's average ping is too high, kick them.
                if (averagePing >= maxAveragePing) {
                    KickUtil.kickPlayer(player, event, "Player's average ping exceeds the limits (Average: " + averagePing + "ms)");
                    return;
                }

                // If the client's ping deviation is too high, kick them.
                if (pingDeviation >= maxPingDeviation && !player.isDead()
                        && timeSinceRespawn > 1000L && timeSinceTeleport > 1000L) {
                    KickUtil.kickPlayer(player, event, "Player's ping deviation exceeds the limits (Dev: " + pingDeviation + ")");
                    return;
                }
            }

            // If the client has sent multiple negative KeepAlive packets in a row, kick them.
            if (keepAliveId == -1) {
                ++negativeStreak;

                if (negativeStreak >= 5) {
                    KickUtil.kickPlayer(player, event, "Sent multiple negative KeepAlive packets in a row");
                    return;
                }
            } else {
                negativeStreak = 0;
            }

            // If the client has sent more KeepAlive packets than received, kick them.
            if (keepAliveInCount > 4 && keepAliveInCount - keepAliveOutCount > 2) {
                KickUtil.kickPlayer(player, event, "Sent more KeepAlive packets than received "
                        + "(Count: " + keepAliveInCount + "/" + keepAliveOutCount + ")");
                return;
            }

            // If the client has sent a KeepAlive packet that was not sent by the server, kick them.
            if (keepAliveId != 0) {
                if (keepAliveOut.stream().map(Pair::getFirst).noneMatch(first -> first == keepAliveId)) {
                    KickUtil.kickPlayer(player, event, "Sent a KeepAlive"
                            + " packet that was not sent by the server: " + keepAliveId);
                } else {
                    // Remove the KeepAlive packet sent by the server.
                    keepAliveOut.removeIf(pair -> {
                        @NotNull Long first = pair.getFirst();
                        return first == keepAliveId;
                    });
                }
            }
        }
    }

    @Override
    public void handle(@NotNull PacketPlaySendEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        long timeSinceRespawn = playerData.getTicksSince(ActionType.RESPAWN);
        long timeSinceTeleport = playerData.getTicksSince(ActionType.TELEPORT);
        long timeSinceLogin = playerData.getTicksSince(ActionType.LOGIN);

        if (packetType == PacketType.Play.Server.KEEP_ALIVE) {
            @NotNull WrapperPlayServerKeepAlive keepAlive = new WrapperPlayServerKeepAlive(event);
            long keepAliveId = keepAlive.getId();
            long timestamp = System.currentTimeMillis();

            // Adds the KeepAlive packet sent by the server.
            keepAliveOut.add(new Pair<>(keepAliveId, timestamp));
            keepAliveOutCount++;

            // If the client might be cancelling sending KeepAlive packets, kick them.
            if (keepAliveOutCount - keepAliveInCount >= 4 && !player.isDead()
                    && timeSinceLogin > 20000L && timeSinceRespawn > 1000L && timeSinceTeleport > 1000L) {
                KickUtil.kickPlayer(player, event, "Might be cancelling sending KeepAlive packets");
            }
        }
    }
}
