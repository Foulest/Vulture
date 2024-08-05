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
package dev.thomazz.pledge.network.queue;

import dev.thomazz.pledge.network.NetworkMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class MessageQueueHandler extends ChannelOutboundHandlerAdapter {

    private final Deque<NetworkMessage> messageQueue = new ConcurrentLinkedDeque<>();
    private QueueMode mode = QueueMode.PASS;

    @Override
    @SuppressWarnings("ProhibitedExceptionDeclared")
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        switch (mode) {
            case ADD_FIRST:
                messageQueue.addFirst(NetworkMessage.of(msg, promise));
                break;

            case ADD_LAST:
                messageQueue.addLast(NetworkMessage.of(msg, promise));
                break;

            default:
                super.write(ctx, msg, promise);
                break;
        }
    }

    @Override
    @SuppressWarnings("ProhibitedExceptionDeclared")
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        drain(ctx);
        super.close(ctx, promise);
    }

    public void drain(ChannelHandlerContext ctx) {
        while (!messageQueue.isEmpty()) {
            NetworkMessage message = messageQueue.poll();
            ctx.write(message.getMessage(), message.getPromise());
        }

        ctx.flush();
    }
}
