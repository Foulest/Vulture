package io.github.retrooper.packetevents.packetwrappers.api;

/**
 * Packet-Wrappers that can be sent to clients.
 *
 * @author retrooper
 */
public interface SendableWrapper {
    Object asNMSPacket() throws Exception;
}
