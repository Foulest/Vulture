package io.github.retrooper.packetevents.injector;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.impl.PlayerEjectEvent;
import io.github.retrooper.packetevents.event.impl.PlayerInjectEvent;
import io.github.retrooper.packetevents.injector.modern.early.EarlyChannelInjectorModern;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GlobalChannelInjector {

    private ChannelInjector injector;

    public void load() {
        injector = new EarlyChannelInjectorModern();
    }

    public boolean isBound() {
        return injector.isBound();
    }

    public void inject() {
        try {
            injector.inject();
        } catch (Exception ex) {
            PacketEvents.get().getPlugin().getLogger().warning("[packetevents] Failed to inject into the server. The server will now shutdown.");
            Bukkit.getServer().shutdown();
        }
    }

    public void eject() {
        injector.eject();
    }

    public void injectPlayer(Player player) {
        PlayerInjectEvent injectEvent = new PlayerInjectEvent(player);
        PacketEvents.get().callEvent(injectEvent);

        if (!injectEvent.isCancelled()) {
            injector.injectPlayer(player);
        }
    }

    public void ejectPlayer(Player player) {
        PlayerEjectEvent ejectEvent = new PlayerEjectEvent(player);
        PacketEvents.get().callEvent(ejectEvent);

        if (!ejectEvent.isCancelled()) {
            injector.ejectPlayer(player);
        }
    }

    public boolean hasInjected(Player player) {
        return injector.hasInjected(player);
    }

    public void writePacket(Object ch, Object rawNMSPacket) {
        injector.writePacket(ch, rawNMSPacket);
    }

    public void flushPackets(Object ch) {
        injector.flushPackets(ch);
    }

    public void sendPacket(Object ch, Object rawNMSPacket) {
        injector.sendPacket(ch, rawNMSPacket);
    }
}
