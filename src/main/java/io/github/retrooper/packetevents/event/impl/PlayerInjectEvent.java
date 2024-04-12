package io.github.retrooper.packetevents.event.impl;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.PacketEvent;
import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import io.github.retrooper.packetevents.event.eventtypes.CancellableEvent;
import io.github.retrooper.packetevents.event.eventtypes.PlayerEvent;
import io.github.retrooper.packetevents.utils.netty.channel.ChannelUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

/**
 * The {@code PlayerInjectEvent} event is fired whenever a player is injected.
 * A player is injected by PacketEvents whenever they join the server.
 * This class implements {@link CancellableEvent} and {@link PlayerEvent}.
 *
 * @author retrooper
 * @see <a href="https://github.com/retrooper/packetevents/blob/dev/src/main/java/io/github/retrooper/packetevents/handler/PacketHandlerInternal.java">https://github.com/retrooper/packetevents/blob/dev/src/main/java/io/github/retrooper/packetevents/handler/PacketHandlerInternal.java</a>
 * @since 1.6.9
 */
public final class PlayerInjectEvent extends PacketEvent implements CancellableEvent, PlayerEvent {

    private final Player player;
    private final InetSocketAddress address;
    private boolean cancelled;

    public PlayerInjectEvent(Player player) {
        this.player = player;
        this.address = ChannelUtils.getSocketAddress(PacketEvents.get().getPlayerUtils().getChannel(player));
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean value) {
        cancelled = value;
    }

    /**
     * This method returns the bukkit player object of the player being injected.
     * This player might not be fully initialized.
     *
     * @return Injected Player.
     */
    @Nullable
    @Override
    public Player getPlayer() {
        return player;
    }

    /**
     * This method returns the socket address injecting player.
     *
     * @return Socket address of the injecting player.
     */
    @Nullable
    public InetSocketAddress getSocketAddress() {
        return address;
    }

    @Override
    public void call(@NotNull PacketListenerAbstract listener) {
        listener.onPlayerInject(this);
    }

    @Override
    public boolean isInbuilt() {
        return true;
    }
}
