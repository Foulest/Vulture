package io.github.retrooper.packetevents.event.impl;

import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code PacketHandshakeReceiveEvent} event is fired whenever the a HANDSHAKE packet is received from a client.
 * The {@code PacketHandshakeReceiveEvent} has no Bukkit player, the player object is null in this state.
 * Use the {@link #getSocketAddress()} to identify who sends the packet.
 *
 * @author retrooper
 * @see <a href="https://wiki.vg/Protocol#Handshaking">https://wiki.vg/Protocol#Handshaking</a>
 * @since 1.8
 */
public class PacketHandshakeReceiveEvent extends CancellableNMSPacketEvent {

    public PacketHandshakeReceiveEvent(Object channel, NMSPacket packet) {
        super(channel, packet);
    }

    @Override
    public void call(@NotNull PacketListenerAbstract listener) {
        if (listener.clientSidedLoginAllowance == null
                || listener.clientSidedLoginAllowance.contains(getPacketId())) {
            listener.onPacketHandshakeReceive(this);
        }
    }
}
