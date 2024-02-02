package dev._2lstudios.hamsterapi.wrappers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
public class EventWrapper implements Cancellable {

    private final PacketWrapper packet;
    private final ByteBuf byteBuf;
    private final ChannelHandlerContext channelHandlerContext;
    private final PlayerData playerData;
    private final Player player;
    @Setter
    private boolean cancelled;
    private boolean closed;

    @Contract(pure = true)
    public EventWrapper(@NotNull PlayerData playerData,
                        ChannelHandlerContext channelHandlerContext,
                        PacketWrapper packet) {
        this.playerData = playerData;
        this.channelHandlerContext = channelHandlerContext;
        this.packet = packet;
        player = playerData.getPlayer();
        byteBuf = null;
    }

    @Contract(pure = true)
    public EventWrapper(@NotNull PlayerData playerData,
                        ChannelHandlerContext channelHandlerContext,
                        ByteBuf byteBuf) {
        this.playerData = playerData;
        this.channelHandlerContext = channelHandlerContext;
        this.byteBuf = byteBuf;
        player = playerData.getPlayer();
        packet = null;
    }

    public ChannelPipeline getPipeline() {
        return channelHandlerContext.pipeline();
    }

    public void close() {
        channelHandlerContext.close();
        closed = true;
    }

    public ByteBufWrapper getByteWrapper() {
        return new ByteBufWrapper(byteBuf);
    }
}
