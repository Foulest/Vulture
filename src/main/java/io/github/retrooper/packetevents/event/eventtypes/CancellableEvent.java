package io.github.retrooper.packetevents.event.eventtypes;

import io.github.retrooper.packetevents.event.impl.PacketPlayReceiveEvent;

/**
 * Every event that supports cancellation should implement this interface.
 * PacketEvents' event system lets the highest priority listener be have the highest priority
 * in deciding whether the event will cancel.
 * This means an event with a lower priority than the higher priority one would not be able to decide.
 * Cancelling the event means the action assosiated with the event will be cancelled.
 * For example, cancelling the {@link PacketPlayReceiveEvent}
 * will prevent minecraft from processing the incoming packet.
 *
 * @author retrooper
 * @see PacketPlayReceiveEvent
 * @since 1.7
 */
public interface CancellableEvent {

    /**
     * This method returns if the event will be cancelled.
     *
     * @return Will the event be cancelled.
     */
    boolean isCancelled();

    /**
     * Cancel or proceed with the event.
     *
     * @param val Is the event cancelled
     */
    void setCancelled(boolean val);
}
