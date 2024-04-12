package io.github.retrooper.packetevents.event.eventtypes;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;

public abstract class CancellableNMSPacketEvent extends NMSPacketEvent implements CancellableEvent {

    private boolean cancelled;

    public CancellableNMSPacketEvent(Object channel, NMSPacket packet) {
        super(channel, packet);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean value) {
        cancelled = value;
    }
}
