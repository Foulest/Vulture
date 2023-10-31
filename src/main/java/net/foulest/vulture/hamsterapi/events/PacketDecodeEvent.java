package net.foulest.vulture.hamsterapi.events;

import net.foulest.vulture.hamsterapi.wrappers.ByteBufWrapper;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import net.foulest.vulture.data.PlayerData;

@Getter
public class PacketDecodeEvent extends PacketEvent {

    private final ByteBufWrapper byteBuf;

    public PacketDecodeEvent(ChannelHandlerContext channelHandlerContext, PlayerData playerData,
                             ByteBufWrapper byteBuf, boolean async) {
        super(channelHandlerContext, playerData, async);
        this.byteBuf = byteBuf;
    }
}
