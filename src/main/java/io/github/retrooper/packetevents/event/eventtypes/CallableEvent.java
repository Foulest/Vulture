package io.github.retrooper.packetevents.event.eventtypes;

import io.github.retrooper.packetevents.event.PacketEvent;
import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import io.github.retrooper.packetevents.event.impl.PacketPlayReceiveEvent;

/**
 * The {@link PacketEvent} implements this interface.
 * Every inbuilt event should implement the {@link #call(PacketListenerAbstract)} method.
 * If you are making a custom event, don't implement this.
 * The {@link PacketListenerAbstract#onPacketEventExternal(PacketEvent)} method is called for every event that is not in-built.
 * including custom events.
 *
 * @author retrooper
 * @see PacketPlayReceiveEvent
 * @since 1.8
 */
public interface CallableEvent {

    void call(PacketListenerAbstract listener);
}
