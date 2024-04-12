package io.github.retrooper.packetevents.event.impl;

import io.github.retrooper.packetevents.event.PacketEvent;
import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import io.github.retrooper.packetevents.event.eventtypes.CancellableEvent;
import io.github.retrooper.packetevents.event.eventtypes.PlayerEvent;
import io.github.retrooper.packetevents.utils.bytebuf.ByteBufWrapper;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class PacketDecodeEvent extends PacketEvent implements CancellableEvent, PlayerEvent {

    private final ChannelHandlerContext channelHandlerContext;
    private final Player player;
    private final ByteBufWrapper byteBuf;
    private final boolean async;
    private boolean cancelled;

    public PacketDecodeEvent(ChannelHandlerContext channelHandlerContext, Player player,
                             ByteBufWrapper byteBuf, boolean async) {
        this.channelHandlerContext = channelHandlerContext;
        this.player = player;
        this.byteBuf = byteBuf;
        this.async = async;
    }

    @Override
    public void call(@NotNull PacketListenerAbstract listener) {
        listener.onPacketDecode(this);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean val) {
        cancelled = val;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
