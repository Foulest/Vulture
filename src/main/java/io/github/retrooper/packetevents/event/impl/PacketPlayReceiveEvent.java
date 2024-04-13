package io.github.retrooper.packetevents.event.impl;

import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.event.eventtypes.PlayerEvent;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code PacketPlayReceiveEvent} event is fired whenever a PLAY packet is
 * received from any connected client.
 * Cancelling this event will result in preventing minecraft from processing the incoming packet.
 * It would be as if the player never sent the packet from the server's view.
 *
 * @author retrooper
 * @see <a href="https://wiki.vg/Protocol#Play">https://wiki.vg/Protocol#Play</a>
 * @since 1.2.6
 */
public final class PacketPlayReceiveEvent extends CancellableNMSPacketEvent implements PlayerEvent {

    private final Player player;

    public PacketPlayReceiveEvent(Player player, Object channel, NMSPacket packet) {
        super(channel, packet);
        this.player = player;
    }

    /**
     * This method returns the bukkit player object of the packet sender.
     * The player object might be null during early packets.
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
            listener.onPacketPlayReceive(this);
        }
    }
}
