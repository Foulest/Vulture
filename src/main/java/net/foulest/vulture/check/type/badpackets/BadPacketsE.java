package net.foulest.vulture.check.type.badpackets;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.resourcepackstatus.WrappedPacketInResourcePackStatus;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;

@CheckInfo(name = "BadPackets (E)", type = CheckType.BADPACKETS,
        acceptsServerPackets = true, punishable = false,
        description = "Detects sending invalid ResourcePackStatus packets.")
public class BadPacketsE extends Check {

    private boolean accepted;
    private int packetsSent;
    private int packetsReceived;

    public BadPacketsE(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (packetId == PacketType.Play.Server.RESOURCE_PACK_SEND) {
            ++packetsSent;

        } else if (packetId == PacketType.Play.Client.RESOURCE_PACK_STATUS) {
            WrappedPacketInResourcePackStatus resourcePackStatus = new WrappedPacketInResourcePackStatus(nmsPacket);
            WrappedPacketInResourcePackStatus.ResourcePackStatus status = resourcePackStatus.getStatus();

            // Keeps track of packets received.
            if (status != WrappedPacketInResourcePackStatus.ResourcePackStatus.ACCEPTED) {
                ++packetsReceived;
            }

            // Detects receiving more packets than sent.
            if (packetsReceived > packetsSent) {
                KickUtil.kickPlayer(player, event, "Sent more ResourcePackStatus packets than received");
                return;
            }

            // Detects sending two ACCEPTED packets in a row.
            if (status == WrappedPacketInResourcePackStatus.ResourcePackStatus.ACCEPTED) {
                if (accepted) {
                    KickUtil.kickPlayer(player, event, "Sent two ResourcePackStatus ACCEPTED packets in a row");
                    return;
                }
                accepted = true;
            } else {
                accepted = false;
            }
        }
    }
}
