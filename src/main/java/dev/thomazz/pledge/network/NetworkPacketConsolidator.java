package dev.thomazz.pledge.network;

import dev.thomazz.pledge.packet.PacketFiltering;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.ArrayDeque;
import java.util.Queue;

// Prevents asynchronously sent packets to land outside the start and end ping interval
public class NetworkPacketConsolidator extends ChannelOutboundHandlerAdapter {

    private final Queue<NetworkMessage> messageQueue = new ArrayDeque<>();
    private boolean started = false;
    private boolean open = true;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
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

    public void drain(ChannelHandlerContext ctx) {
        while (!messageQueue.isEmpty()) {
            NetworkMessage message = messageQueue.poll();
            ctx.write(message.getMessage(), message.getPromise());
        }

        ctx.flush();
    }
}
