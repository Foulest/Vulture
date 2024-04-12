package io.github.retrooper.packetevents.injector.modern.early;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.injector.modern.PlayerChannelHandlerModern;
import io.github.retrooper.packetevents.injector.modern.PlayerDecodeHandlerModern;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

@Getter
public class PEChannelInitializerModern extends ChannelInitializer<Channel> {

    private final ChannelInitializer<?> oldChannelInitializer;
    private Method initChannelMethod;

    public PEChannelInitializerModern(ChannelInitializer<?> oldChannelInitializer) {
        this.oldChannelInitializer = oldChannelInitializer;
        load();
    }

    public static void postInitChannel(@NotNull Channel channel) {
        if (channel.getClass().equals(NioSocketChannel.class)
                || channel.getClass().equals(EpollSocketChannel.class)) {
            PlayerChannelHandlerModern channelHandler = new PlayerChannelHandlerModern();
            PlayerDecodeHandlerModern decodeHandler = new PlayerDecodeHandlerModern();

            if (channel.pipeline().get("packet_handler") != null) {
                String handlerName = PacketEvents.get().getHandlerName();

                if (channel.pipeline().get(handlerName) == null) {
                    channel.pipeline().addBefore("packet_handler", handlerName, channelHandler);
                }
            }

            if (channel.pipeline().get("splitter") != null) {
                String handlerName = PacketEvents.get().getHandlerName() + "-decoder";

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
    protected void initChannel(Channel channel) throws Exception {
        initChannelMethod.invoke(oldChannelInitializer, channel);
        postInitChannel(channel);
    }
}
