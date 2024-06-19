package io.github.retrooper.packetevents.event;

/**
 * The priority of packet listeners affect the order they will be invoked in.
 * The lowest priority listeners are invoked first, the most high ones are invoked last.
 * The most high priority listener has the final decider on an event being cancelled.
 * This priority can be specified in the PacketListenerAbstract constructor.
 * If you don't specify a priority in the constructor, it will use the {@link #NORMAL} priority.
 *
 * @author retrooper
 * @since 1.8
 */
public enum PacketListenerPriority {
    /**
     * Listener is of very low importance.
     * This listener will be run first.
     */
    LOWEST,

    /**
     * Listener is of low importance.
     */
    LOW,

    /**
     * Default listener priority.
     * Listener is neither important nor unimportant and may run normally.
     */
    NORMAL,

    /**
     * Listener is of high importance.
     */
    HIGH,

    /**
     * Listener is of critical importance and wants to decide the cancellation of an event.
     */
    HIGHEST,

    /**
     * Listener is purely trying to decide the cancellation of an event.
     * This listener should be run last.
     */
    MONITOR;

    public static PacketListenerPriority getById(byte id) {
        return values()[id];
    }

    public byte getId() {
        return (byte) ordinal();
    }
}