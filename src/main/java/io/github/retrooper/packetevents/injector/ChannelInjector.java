package io.github.retrooper.packetevents.injector;

import org.bukkit.entity.Player;

public interface ChannelInjector {

    default boolean isBound() {
        return true;
    }

    void inject() throws IllegalAccessException;

    void eject();

    void injectPlayer(Player player);

    void ejectPlayer(Player player);

    boolean hasInjected(Player player);

    void writePacket(Object channel, Object rawNMSPacket);

    void flushPackets(Object channel);

    void sendPacket(Object channel, Object rawNMSPacket);
}
