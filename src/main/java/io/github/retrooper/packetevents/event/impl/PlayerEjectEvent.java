package io.github.retrooper.packetevents.event.impl;

import io.github.retrooper.packetevents.event.PacketEvent;
import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import io.github.retrooper.packetevents.event.eventtypes.CancellableEvent;
import io.github.retrooper.packetevents.event.eventtypes.PlayerEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code PlayerEjectEvent} event is fired whenever a player is ejected.
 * This class implements {@link CancellableEvent} and {@link PlayerEvent}.
 *
 * @author retrooper
 * @see <a href="https://github.com/retrooper/packetevents/blob/dev/src/main/java/io/github/retrooper/packetevents/handler/PacketHandlerInternal.java">https://github.com/retrooper/packetevents/blob/dev/src/main/java/io/github/retrooper/packetevents/handler/PacketHandlerInternal.java</a>
 * @since 1.6.9
 */
public final class PlayerEjectEvent extends PacketEvent implements CancellableEvent, PlayerEvent {

    private final Player player;
    private boolean cancelled;

    public PlayerEjectEvent(Player player) {
        this.player = player;
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
     * This method returns the bukkit player object of the player being ejected.
     * The player object is guaranteed to NOT be null.
     *
     * @return Ejected player.
     */
    @NotNull
    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void call(@NotNull PacketListenerAbstract listener) {
        listener.onPlayerEject(this);
    }

    @Override
    public boolean isInbuilt() {
        return true;
    }
}
