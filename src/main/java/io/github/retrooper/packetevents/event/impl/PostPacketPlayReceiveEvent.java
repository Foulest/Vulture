package io.github.retrooper.packetevents.event.impl;

import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import io.github.retrooper.packetevents.event.eventtypes.NMSPacketEvent;
import io.github.retrooper.packetevents.event.eventtypes.PlayerEvent;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code PostPacketPlayReceiveEvent} event is fired after all PacketEvents listeners finished processing
 * the {@code PacketPlayReceiveEvent}. This event won't be called if the PacketPlayReceiveEvent event was cancelled.
 * You cannot cancel this event.
 *
 * @author retrooper
 * @see <a href="https://wiki.vg/Protocol#Serverbound_4">https://wiki.vg/Protocol#Serverbound_4</a>
 * @since 1.7
 */
public class PostPacketPlayReceiveEvent extends NMSPacketEvent implements PlayerEvent {

    private final Player player;

    public PostPacketPlayReceiveEvent(Player player, Object channel, NMSPacket packet) {
        super(channel, packet);
        this.player = player;
    }

    /**
     * This method returns the bukkit player object of the packet sender.
     *
     * @return Packet sender.
     */
    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void call(@NotNull PacketListenerAbstract listener) {
        if (listener.clientSidedPlayAllowance == null
                || listener.clientSidedPlayAllowance.contains(getPacketId())) {
            listener.onPostPacketPlayReceive(this);
        }
    }
}
