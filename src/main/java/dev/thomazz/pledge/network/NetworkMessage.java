package dev.thomazz.pledge.network;

import io.netty.channel.ChannelPromise;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NetworkMessage {

    private final Object message;
    private final ChannelPromise promise;

    @Contract("_, _ -> new")
    public static @NotNull NetworkMessage of(Object message, ChannelPromise promise) {
        return new NetworkMessage(message, promise);
    }
}
