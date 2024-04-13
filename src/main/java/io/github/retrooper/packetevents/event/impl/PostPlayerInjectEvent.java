package io.github.retrooper.packetevents.event.impl;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.PacketEvent;
import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import io.github.retrooper.packetevents.event.eventtypes.PlayerEvent;
import io.github.retrooper.packetevents.utils.netty.channel.ChannelUtils;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

/**
 * The {@code PostPlayerInjectEvent} event is fired after a successful injection.
 * Use the {@link #isAsync()} method to figure out if is being called sync or async.
 * A player is injected by PacketEvents each time they join the server.
 *
 * @author retrooper
 * @see <a href="https://github.com/retrooper/packetevents/blob/dev/src/main/java/io/github/retrooper/packetevents/handler/PacketHandlerInternal.java">https://github.com/retrooper/packetevents/blob/dev/src/main/java/io/github/retrooper/packetevents/handler/PacketHandlerInternal.java</a>
 * @since 1.3
 */
@Getter
@AllArgsConstructor
public class PostPlayerInjectEvent extends PacketEvent implements PlayerEvent {

    private final Player player;
    private final boolean async;

    /**
     * This method returns the bukkit player object of the player that has been injected.
     * The player is guaranteed to not be null.
     *
     * @return Injected Player.
     */
    @Override
    public Player getPlayer() {
        return player;
    }

    /**
     * This method returns the cached netty channel of the player.
     *
     * @return Netty channel of the injected player.
     */
    public Object getChannel() {
        return PacketEvents.get().getPlayerUtils().getChannel(player);
    }

    public InetSocketAddress getSocketAddress() {
        return ChannelUtils.getSocketAddress(getChannel());
    }

    /**
     * This method returns the ClientVersion of the injected player.
     *
     * @return ClientVersion of injected player.
     * @see ClientVersion
     */
    public ClientVersion getClientVersion() {
        return PacketEvents.get().getPlayerUtils().getClientVersion(player);
    }

    @Override
    public void call(@NotNull PacketListenerAbstract listener) {
        listener.onPostPlayerInject(this);
    }

    @Override
    public boolean isInbuilt() {
        return true;
    }
}
