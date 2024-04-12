package io.github.retrooper.packetevents.event.impl;

import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code PacketStatusReceiveEvent} event is fired whenever the server receives a STATUS packet from a client.
 * The {@code PacketStatusSendEvent} has no Bukkit player, the player object is null in this state.
 * Use the {@link #getSocketAddress()} to identify who sends the packet.
 *
 * @author retrooper
 * @see <a href="https://wiki.vg/Protocol#Status">https://wiki.vg/Protocol#Status</a>
 * @since 1.8
 */
public class PacketStatusReceiveEvent extends CancellableNMSPacketEvent {

    public PacketStatusReceiveEvent(Object channel, NMSPacket packet) {
        super(channel, packet);
    }

    @Override
    public void call(@NotNull PacketListenerAbstract listener) {
        if (listener.clientSidedStatusAllowance == null
                || listener.clientSidedStatusAllowance.contains(getPacketId())) {
            listener.onPacketStatusReceive(this);
        }
    }
}
