package io.github.retrooper.packetevents.exceptions;

import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.reflection.ClassUtil;

/**
 * An exception thrown by PacketEvents when a wrapper is used although it isn't supported on the local server version.
 * Make sure to decompile a wrapper before using it, if you are unsure if it is supported through out all the server versions.
 * We override a {@link WrappedPacket#isSupported()} method if it isn't supported on all server versions.
 *
 * @author retrooper
 * @see WrappedPacket#isSupported()
 * @see WrappedPacket.SupportedVersions
 * @since 1.7
 */
public class WrapperUnsupportedUsageException extends RuntimeException {

    public WrapperUnsupportedUsageException(String message) {
        super(message);
    }

    public WrapperUnsupportedUsageException(Class<? extends WrappedPacket> wrapperClass) {
        this("You are using a packet wrapper which happens to be unsupported on the local server version."
                + " Packet wrapper you attempted to use: " + ClassUtil.getClassSimpleName(wrapperClass));
    }
}
