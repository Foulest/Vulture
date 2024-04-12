package io.github.retrooper.packetevents.injector.modern;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.processor.PacketProcessorInternal;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@ChannelHandler.Sharable
public class PlayerChannelHandlerModern extends ChannelDuplexHandler {

    /**
     * Associated player.
     * This is null until we inject the player.
     */
    public volatile Player player;

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, Object packet) throws Exception {
        PacketProcessorInternal.PacketData data = PacketEvents.get().getInternalPacketProcessor().read(player, ctx.channel(), packet);

        if (data.packet != null) {
            super.channelRead(ctx, data.packet);
            PacketEvents.get().getInternalPacketProcessor().postRead(player, ctx.channel(), data.packet);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
        if (packet instanceof ByteBuf) {
            // Ignore ByteBufs!
            super.write(ctx, packet, promise);
            return;
        }

        PacketProcessorInternal.PacketData data = PacketEvents.get().getInternalPacketProcessor().write(player, ctx.channel(), packet);

        if (data.postAction != null) {
            promise.addListener(f -> data.postAction.run());
        }

        if (data.packet != null) {
            super.write(ctx, data.packet, promise);
            PacketEvents.get().getInternalPacketProcessor().postWrite(player, ctx.channel(), data.packet);
        }
    }
}
