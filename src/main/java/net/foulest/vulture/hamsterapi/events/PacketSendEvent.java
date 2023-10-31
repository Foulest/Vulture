package net.foulest.vulture.hamsterapi.events;

import net.foulest.vulture.hamsterapi.wrappers.PacketWrapper;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import net.foulest.vulture.data.PlayerData;

@Getter
public class PacketSendEvent extends PacketEvent {

    private final PacketWrapper packet;

    public PacketSendEvent(ChannelHandlerContext channelHandlerContext, PlayerData playerData,
                           PacketWrapper packet, boolean async) {
        super(channelHandlerContext, playerData, async);
        this.packet = packet;
    }
}
