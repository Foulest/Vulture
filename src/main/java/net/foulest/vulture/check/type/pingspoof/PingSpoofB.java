package net.foulest.vulture.check.type.pingspoof;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.keepalive.WrappedPacketInKeepAlive;
import io.github.retrooper.packetevents.packetwrappers.play.out.keepalive.WrappedPacketOutKeepAlive;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.data.EvictingList;
import net.foulest.vulture.util.data.Pair;

@CheckInfo(name = "PingSpoof (B)", type = CheckType.PINGSPOOF,
        acceptsServerPackets = true, punishable = false,
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

    public PingSpoofB(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        long timeSinceRespawn = playerData.getTimeSince(ActionType.RESPAWN);
        long timeSinceTeleport = playerData.getTimeSince(ActionType.TELEPORT);
        long timeSinceLogin = playerData.getTimeSince(ActionType.LOGIN);

        if (packetId == PacketType.Play.Server.KEEP_ALIVE) {
            WrappedPacketOutKeepAlive keepAlive = new WrappedPacketOutKeepAlive(nmsPacket);
            keepAliveOut.add(new Pair<>(keepAlive.getId(), timestamp));
            keepAliveOutCount++;

            // If the client might be cancelling sending KeepAlive packets, kick them.
            if (keepAliveOutCount - keepAliveInCount >= 4 && !player.isDead()
                    && timeSinceLogin > 20000L && timeSinceRespawn > 1000L && timeSinceTeleport > 1000L) {
                KickUtil.kickPlayer(player, event, "Might be cancelling sending KeepAlive packets");
            }

        } else if (packetId == PacketType.Play.Client.KEEP_ALIVE) {
            WrappedPacketInKeepAlive keepAlive = new WrappedPacketInKeepAlive(nmsPacket);
            keepAliveInCount++;

            if (keepAliveOut.isEmpty() || keepAliveOut.size() == 1) {
                return;
            }

            // Calculates the ping, average ping, and ping deviation of the client.
            long ping = timestamp - keepAliveOut.getLast().getY();
            pingValues.add(ping);
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
            if (keepAlive.getId() == -1) {
                if (++negativeStreak >= 5) {
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
            if (keepAlive.getId() != 0) {
                if (keepAliveOut.stream().noneMatch(pair -> pair.getX() == keepAlive.getId())) {
                    KickUtil.kickPlayer(player, event, "Sent a KeepAlive packet that was not sent by the server: "
                            + keepAlive.getId());
                } else {
                    // Remove the KeepAlive packet sent by the server.
                    keepAliveOut.removeIf(pair -> pair.getX() == keepAlive.getId());
                }
            }
        }
    }
}
