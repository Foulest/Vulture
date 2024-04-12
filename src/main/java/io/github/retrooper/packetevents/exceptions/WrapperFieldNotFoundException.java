package io.github.retrooper.packetevents.exceptions;

import io.github.retrooper.packetevents.utils.reflection.ClassUtil;
import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown by PacketEvents when a wrapper fails
 * to find a field.
 *
 * @author retrooper
 * @see io.github.retrooper.packetevents.packetwrappers.WrappedPacket#read(int, Class)
 * @see io.github.retrooper.packetevents.packetwrappers.WrappedPacket#write(Class, int, Object)
 * @since 1.6.9
 */
public class WrapperFieldNotFoundException extends RuntimeException {

    public WrapperFieldNotFoundException(String message) {
        super(message);
    }

    public WrapperFieldNotFoundException(@NotNull Class<?> packetClass, Class<?> type, int index) {
        this("PacketEvents failed to find a " + ClassUtil.getClassSimpleName(type)
                + " indexed " + index + " by its type in the " + packetClass.getName() + " class!");
    }
}
