package io.github.retrooper.packetevents.exceptions;

/**
 * An exception thrown by PacketEvents when the {@link io.github.retrooper.packetevents.PacketEvents#load}
 * method fails to execute without any exceptions.
 * Resulting in PacketEvents failing to load.
 *
 * @author retrooper
 * @since 1.7.6
 */
public class PacketEventsLoadFailureException extends RuntimeException {

    public PacketEventsLoadFailureException(String message) {
        super(message);
    }

    public PacketEventsLoadFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public PacketEventsLoadFailureException() {
        this("PacketEvents failed to load...");
    }

    public PacketEventsLoadFailureException(Throwable cause) {
        this("PacketEvents failed to load...", cause);
    }
}
