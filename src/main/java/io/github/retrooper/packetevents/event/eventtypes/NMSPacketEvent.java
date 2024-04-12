package io.github.retrooper.packetevents.event.eventtypes;

import io.github.retrooper.packetevents.event.PacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.utils.netty.channel.ChannelUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

/**
 * The {@code NMSPacketEvent} abstract class represents an event that has to do with an actual packet.
 * Don't mix this up with {@link io.github.retrooper.packetevents.event.PacketEvent}.
 * The PacketEvent class represents an event that belongs to PacketEvent's packet system.
 *
 * @author retrooper
 * @since 1.8
 */
@Getter
public abstract class NMSPacketEvent extends PacketEvent {

    private final Object channel;
    private final InetSocketAddress socketAddress;
    private final byte packetId;
    protected NMSPacket packet;

    public NMSPacketEvent(Object channel, @NotNull NMSPacket packet) {
        this.channel = channel;
        this.socketAddress = ChannelUtils.getSocketAddress(channel);
        this.packet = packet;
        packetId = PacketType.packetIDMap.getOrDefault(packet.getRawNMSPacket().getClass(), PacketType.INVALID);
    }

    /**
     * Get the associated player's socket address.
     *
     * @return Socket address of the associated player.
     */
    public final InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    /**
     * Get the NMS packet.
     *
     * @return Get NMS packet.
     */
    public final NMSPacket getNMSPacket() {
        return packet;
    }

    /**
     * Update the NMS Packet.
     *
     * @param packet NMS Object
     */
    public final void setNMSPacket(NMSPacket packet) {
        this.packet = packet;
    }

    @Override
    public boolean isInbuilt() {
        return true;
    }
}
