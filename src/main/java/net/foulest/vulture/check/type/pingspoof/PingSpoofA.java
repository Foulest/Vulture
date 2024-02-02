package net.foulest.vulture.check.type.pingspoof;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.transaction.WrappedPacketInTransaction;
import io.github.retrooper.packetevents.packetwrappers.play.out.transaction.WrappedPacketOutTransaction;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.data.EvictingList;
import net.foulest.vulture.util.data.Pair;

@CheckInfo(name = "PingSpoof (A)", type = CheckType.PINGSPOOF,
        acceptsServerPackets = true, punishable = false,
        description = "Detects clients modifying Transaction packets.")
public class PingSpoofA extends Check {

    private final EvictingList<Pair<Short, Long>> transactionsOut = new EvictingList<>(500);
    private int transactionsInCount;
    private int transactionsOutCount;

    public PingSpoofA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (packetId == PacketType.Play.Server.TRANSACTION) {
            WrappedPacketOutTransaction transaction = new WrappedPacketOutTransaction(nmsPacket);
            transactionsOut.add(new Pair<>(transaction.getActionNumber(), timestamp));
            transactionsOutCount++;

            // If the client might be cancelling sending Transaction packets, kick them.
            // The cancelling streak threshold is 385, which roughly equates to 10 seconds.
            if (transactionsOutCount - transactionsInCount >= 385) {
                if (!playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8)) {
                    return;
                }

                transactionsOutCount = 0; // Resets the count to avoid kicking players twice.
                KickUtil.kickPlayer(player, event, "Might be cancelling sending Transaction packets");
            }

        } else if (packetId == PacketType.Play.Client.TRANSACTION) {
            WrappedPacketInTransaction transaction = new WrappedPacketInTransaction(nmsPacket);
            transactionsInCount++;

            // If the client has sent more Transaction packets than received, kick them.
            if (transactionsInCount > transactionsOutCount) {
                KickUtil.kickPlayer(player, event, "Sent more Transaction packets than received");
                return;
            }

            // If the client has sent a Transaction packet that was not sent by the server, kick them.
            if (transactionsOut.stream().noneMatch(pair -> pair.getX() == transaction.getActionNumber())) {
                if (playerData.getTimeSince(ActionType.LOGIN) < 1000
                        || playerData.getTimeSince(ActionType.RESPAWN) < 1000
                        || playerData.getTimeSince(ActionType.TELEPORT) < 1000) {
                    return;
                }

                KickUtil.kickPlayer(player, event, "Sent a Transaction packet that was not sent by the server: "
                        + transaction.getActionNumber());
            } else {
                // Remove the Transaction packet sent by the server.
                transactionsOut.removeIf(pair -> pair.getX() == transaction.getActionNumber());
            }
        }
    }
}
