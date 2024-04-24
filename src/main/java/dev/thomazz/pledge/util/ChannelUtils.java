package dev.thomazz.pledge.util;

import io.netty.channel.Channel;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class ChannelUtils {

    public void runInEventLoop(@NotNull Channel channel, Runnable runnable) {
        if (!channel.eventLoop().inEventLoop()) {
            channel.eventLoop().execute(runnable);
        } else {
            runnable.run();
        }
    }
}
