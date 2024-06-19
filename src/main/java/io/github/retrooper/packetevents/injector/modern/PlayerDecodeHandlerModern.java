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
import io.github.retrooper.packetevents.event.impl.PacketDecodeEvent;
import io.github.retrooper.packetevents.utils.bytebuf.ByteBufWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerDecodeHandlerModern extends ByteToMessageDecoder {

    /**
     * Associated player.
     * This is null until we inject the player.
     */
    public final AtomicReference<Player> player = new AtomicReference<>();

    @Override
    public void decode(@NotNull ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        Player currentPlayer = player.get();
        ByteBufWrapper byteBufWrapper = new ByteBufWrapper(byteBuf);
        PacketDecodeEvent event = new PacketDecodeEvent(ctx, currentPlayer, byteBufWrapper, false);
        PacketEvents.get().getEventManager().callEvent(event);

        if (!event.isCancelled()) {
            list.add(byteBuf.readBytes(byteBuf.readableBytes()));
        } else {
            byteBuf.skipBytes(byteBuf.readableBytes());
        }
    }
}
