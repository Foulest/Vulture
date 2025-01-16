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
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.ToString;
import net.foulest.pledge.packet.PacketFiltering;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;

// Prevents asynchronously sent packets to land outside the start and end ping interval
@ToString
public class NetworkPacketConsolidator extends ChannelOutboundHandlerAdapter {

    private final Queue<NetworkMessage> messageQueue = new ArrayDeque<>();
    private boolean started;
    private boolean open = true;

    @Override
    @SuppressWarnings("ProhibitedExceptionDeclared")
    public void write(ChannelHandlerContext ctx, @NotNull Object msg, @NotNull ChannelPromise promise) throws Exception {
        // Start with login packet in game state
        if (PacketFiltering.isLoginPacket(msg)) {
            started = true;
        }

        // Check if started, some packets are whitelisted from being queued
        if (started && !open && !PacketFiltering.isWhitelistedFromQueue(msg)) {
            messageQueue.add(NetworkMessage.of(msg, promise));
            return;
        }

        super.write(ctx, msg, promise);
    }

    public void open() {
        open = true;
    }

    public void close() {
        open = false;
    }

    public void drain(@NotNull ChannelHandlerContext ctx) {
        while (!messageQueue.isEmpty()) {
            NetworkMessage message = messageQueue.poll();
            ctx.write(message.getMessage(), message.getPromise());
        }

        ctx.flush();
    }
}
