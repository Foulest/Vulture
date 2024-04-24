package dev.thomazz.pledge.network.queue;

import dev.thomazz.pledge.packet.PacketFiltering;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageQueuePrimer extends ChannelOutboundHandlerAdapter {

    private final MessageQueueHandler queueHandler;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // Set queue handler to add last after login
        if (PacketFiltering.isLoginPacket(msg)) {
            queueHandler.setMode(QueueMode.ADD_LAST);
        }

        // Let whitelisted packets pass through the queue
        if (PacketFiltering.isWhitelistedFromQueue(msg)) {
            QueueMode lastMode = queueHandler.getMode();
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
