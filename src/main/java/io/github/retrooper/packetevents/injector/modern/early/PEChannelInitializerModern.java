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
package io.github.retrooper.packetevents.injector.modern.early;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.injector.modern.PlayerChannelHandlerModern;
import io.github.retrooper.packetevents.injector.modern.PlayerDecodeHandlerModern;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Getter
class PEChannelInitializerModern extends ChannelInitializer<Channel> {

    private final ChannelInitializer<?> oldChannelInitializer;
    private Method initChannelMethod;

    PEChannelInitializerModern(ChannelInitializer<?> oldChannelInitializer) {
        this.oldChannelInitializer = oldChannelInitializer;
        load();
    }

    private static void postInitChannel(@NotNull Channel channel) {
        if (channel.getClass().equals(NioSocketChannel.class)
                || channel.getClass().equals(EpollSocketChannel.class)) {
            ChannelHandler channelHandler = new PlayerChannelHandlerModern();
            ChannelHandler decodeHandler = new PlayerDecodeHandlerModern();

            if (channel.pipeline().get("packet_handler") != null) {
                String handlerName = PacketEvents.getInstance().getHandlerName();

                if (channel.pipeline().get(handlerName) == null) {
                    channel.pipeline().addBefore("packet_handler", handlerName, channelHandler);
                }
            }

            if (channel.pipeline().get("splitter") != null) {
                String handlerName = PacketEvents.getInstance().getHandlerName() + "-decoder";

                if (channel.pipeline().get(handlerName) == null) {
                    channel.pipeline().addAfter("splitter", handlerName, decodeHandler);
                }
            }
        }
    }

    private void load() {
        initChannelMethod = Reflection.getMethod(oldChannelInitializer.getClass(), "initChannel", 0);
    }

    @Override
    protected void initChannel(Channel channel) throws InvocationTargetException, IllegalAccessException {
        initChannelMethod.invoke(oldChannelInitializer, channel);
        postInitChannel(channel);
    }
}
