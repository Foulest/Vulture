package dev.thomazz.pledge.network.queue;

import dev.thomazz.pledge.network.NetworkMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.Setter;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@Setter
@Getter
public class MessageQueueHandler extends ChannelOutboundHandlerAdapter {

    private final Deque<NetworkMessage> messageQueue = new ConcurrentLinkedDeque<>();
    private QueueMode mode = QueueMode.PASS;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        switch (mode) {
            case ADD_FIRST:
                messageQueue.addFirst(NetworkMessage.of(msg, promise));
                break;

            case ADD_LAST:
                messageQueue.addLast(NetworkMessage.of(msg, promise));
                break;

            case PASS:
            default:
                super.write(ctx, msg, promise);
                break;
        }
    }

    @Override
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
