package io.github.retrooper.packetevents.event;


import io.github.retrooper.packetevents.event.eventtypes.CallableEvent;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * An event in both of PacketEvents' event systems.
 *
 * @author retrooper
 * @since 1.2.6
 */
@Setter
@Getter
public abstract class PacketEvent implements CallableEvent {

    private long timestamp = System.currentTimeMillis();

    public void callPacketEventExternal(@NotNull PacketListenerAbstract listener) {
        listener.onPacketEventExternal(this);
    }

    public boolean isInbuilt() {
        return false;
    }
}
