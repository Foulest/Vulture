/*
 * This file is part of Pledge - https://github.com/ThomasOM/Pledge
 * Copyright (C) 2021 Thomazz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.foulest.pledge.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.foulest.pledge.PledgeImpl;
import net.foulest.pledge.event.PongReceiveEvent;
import net.foulest.pledge.packet.PingPacketProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@ToString
@RequiredArgsConstructor
public class NetworkPongListener extends ChannelInboundHandlerAdapter {

    private final @NotNull PledgeImpl clientPing;
    private final @NotNull Player player;

    @Override
    @SuppressWarnings("ProhibitedExceptionDeclared")
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PingPacketProvider packetProvider = clientPing.getPacketProvider();

        if (packetProvider.isPong(msg)) {
            int id = packetProvider.idFromPong(msg);
            Bukkit.getServer().getPluginManager().callEvent(new PongReceiveEvent(player, id));
        }

        super.channelRead(ctx, msg);
    }
}
