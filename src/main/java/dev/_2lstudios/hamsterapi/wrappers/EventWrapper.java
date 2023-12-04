package dev._2lstudios.hamsterapi.wrappers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@Getter
public class EventWrapper implements Cancellable {

    private final PacketWrapper packet;
    private final ByteBuf byteBuf;
    private final ChannelHandlerContext channelHandlerContext;
    private final PlayerData playerData;
    private final Player player;
    @Setter
    private boolean cancelled = false;
    private boolean closed = false;

    public EventWrapper(PlayerData playerData, ChannelHandlerContext channelHandlerContext, PacketWrapper packet) {
        this.packet = packet;
        this.channelHandlerContext = channelHandlerContext;
        this.playerData = playerData;
        this.player = playerData.getPlayer();
        this.byteBuf = null;
    }

    public EventWrapper(PlayerData playerData, ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        this.channelHandlerContext = channelHandlerContext;
        this.playerData = playerData;
        this.player = playerData.getPlayer();
        this.packet = null;
        this.byteBuf = byteBuf;
    }

    public ChannelPipeline getPipeline() {
        return this.channelHandlerContext.pipeline();
    }

    public void close() {
        this.channelHandlerContext.close();
        this.closed = true;
    }

    public ByteBufWrapper getByteWrapper() {
        return new ByteBufWrapper(this.byteBuf);
    }
}
