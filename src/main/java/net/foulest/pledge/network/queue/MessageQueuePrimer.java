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
package net.foulest.pledge.network.queue;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.foulest.pledge.packet.PacketFiltering;
import org.jetbrains.annotations.NotNull;

@ToString
@RequiredArgsConstructor
public class MessageQueuePrimer extends ChannelOutboundHandlerAdapter {

    private final @NotNull MessageQueueHandler queueHandler;

    @Override
    @SuppressWarnings("ProhibitedExceptionDeclared")
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // Set queue handler to add last after login
        if (PacketFiltering.isLoginPacket(msg)) {
            queueHandler.setMode(QueueMode.ADD_LAST);
        }

        // Let whitelisted packets pass through the queue
        if (PacketFiltering.isWhitelistedFromQueue(msg)) {
            @NotNull QueueMode lastMode = queueHandler.getMode();
            queueHandler.setMode(QueueMode.PASS);

            try {
                super.write(ctx, msg, promise);
                flush(ctx);
            } finally {
                queueHandler.setMode(lastMode);
            }
            return;
        }

        super.write(ctx, msg, promise);
    }
}
