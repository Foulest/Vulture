package io.github.retrooper.packetevents.utils.netty.channel;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public final class ChannelUtils {

    public static InetSocketAddress getSocketAddress(Object ch) {
        if (ch == null) {
            return null;
        }

        Channel channel = (Channel) ch;
        return ((InetSocketAddress) channel.remoteAddress());
    }
}
