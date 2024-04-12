package io.github.retrooper.packetevents.injector.modern;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.impl.PacketDecodeEvent;
import io.github.retrooper.packetevents.utils.bytebuf.ByteBufWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerDecodeHandlerModern extends ByteToMessageDecoder {

    /**
     * Associated player.
     * This is null until we inject the player.
     */
    public volatile Player player;

    @Override
    public void decode(@NotNull ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        ByteBufWrapper byteBufWrapper = new ByteBufWrapper(byteBuf);
        PacketDecodeEvent event = new PacketDecodeEvent(ctx, player, byteBufWrapper, false);
        PacketEvents.get().getEventManager().callEvent(event);

        if (!event.isCancelled()) {
            list.add(byteBuf.readBytes(byteBuf.readableBytes()));
        } else {
            byteBuf.skipBytes(byteBuf.readableBytes());
        }
    }
}
