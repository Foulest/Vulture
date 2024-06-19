/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
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

import java.util.concurrent.atomic.AtomicReference;

@ChannelHandler.Sharable
public class PlayerChannelHandlerModern extends ChannelDuplexHandler {

    /**
     * Associated player.
     * This is null until we inject the player.
     */
    public final AtomicReference<Player> player = new AtomicReference<>();

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, Object packet) throws Exception {
        Player currentPlayer = player.get();
        PacketProcessorInternal.PacketData data = PacketEvents.get().getInternalPacketProcessor().read(currentPlayer, ctx.channel(), packet);

        if (data.packet != null) {
            super.channelRead(ctx, data.packet);
            PacketEvents.get().getInternalPacketProcessor().postRead(currentPlayer, ctx.channel(), data.packet);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
        if (packet instanceof ByteBuf) {
            // Ignore ByteBufs!
            super.write(ctx, packet, promise);
            return;
        }

        Player currentPlayer = player.get();
        PacketProcessorInternal.PacketData data = PacketEvents.get().getInternalPacketProcessor().write(currentPlayer, ctx.channel(), packet);

        if (data.postAction != null) {
            promise.addListener(f -> data.postAction.run());
        }

        if (data.packet != null) {
            super.write(ctx, data.packet, promise);
            PacketEvents.get().getInternalPacketProcessor().postWrite(currentPlayer, ctx.channel(), data.packet);
        }
    }
}
