package io.github.retrooper.packetevents.event.impl;

import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.event.eventtypes.PostTaskEvent;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code PacketLoginSendEvent} event is fired whenever the a LOGIN packet is being planned to be sent to the client.
 * The {@code PacketLoginSendEvent} has no Bukkit player, the player object is null in this state.
 * Use the {@link #getSocketAddress()} to identify who sends the packet.
 *
 * @author retrooper
 * @see <a href="https://wiki.vg/Protocol#Login">https://wiki.vg/Protocol#Login</a>
 * @since 1.8
 */
public class PacketLoginSendEvent extends CancellableNMSPacketEvent implements PostTaskEvent {

    private Runnable postTask;

    public PacketLoginSendEvent(Object channel, NMSPacket packet) {
        super(channel, packet);
    }

    @Override
    public boolean isPostTaskAvailable() {
        return postTask != null;
    }

    @Override
    public Runnable getPostTask() {
        return postTask;
    }

    @Override
    public void setPostTask(@NotNull Runnable postTask) {
        this.postTask = postTask;
    }

    @Override
    public void call(@NotNull PacketListenerAbstract listener) {
        if (listener.serverSidedLoginAllowance == null
                || listener.serverSidedLoginAllowance.contains(getPacketId())) {
            listener.onPacketLoginSend(this);
        }
    }
}
