package io.github.retrooper.packetevents.event.impl;

import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code PacketLoginReceiveEvent} event is fired whenever the a LOGIN packet is received from a client.
 * The {@code PacketLoginReceiveEvent} has no Bukkit player, the player object is null in this state.
 * Use the {@link #getSocketAddress()} to identify who sends the packet.
 *
 * @author retrooper
 * @see <a href="https://wiki.vg/Protocol#Login">https://wiki.vg/Protocol#Login</a>
 * @since 1.8
 */
public class PacketLoginReceiveEvent extends CancellableNMSPacketEvent {

    public PacketLoginReceiveEvent(Object channel, NMSPacket packet) {
        super(channel, packet);
    }

    @Override
    public void call(@NotNull PacketListenerAbstract listener) {
        if (listener.clientSidedLoginAllowance == null
                || listener.clientSidedLoginAllowance.contains(getPacketId())) {
            listener.onPacketLoginReceive(this);
        }
    }
}
