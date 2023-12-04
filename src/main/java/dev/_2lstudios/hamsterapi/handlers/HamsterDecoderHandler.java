package dev._2lstudios.hamsterapi.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.foulest.vulture.data.PlayerData;
import dev._2lstudios.hamsterapi.events.PacketDecodeEvent;
import dev._2lstudios.hamsterapi.wrappers.ByteBufWrapper;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

import java.util.List;

public class HamsterDecoderHandler extends ByteToMessageDecoder {

    private final Server server;
    private final PluginManager pluginManager;
    private final PlayerData playerData;

    public HamsterDecoderHandler(PlayerData playerData) {
        this.server = playerData.getPlayer().getServer();
        this.pluginManager = server.getPluginManager();
        this.playerData = playerData;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf bytebuf, List<Object> list) {
        ByteBufWrapper byteBufWrapper = new ByteBufWrapper(bytebuf);
        boolean async = !server.isPrimaryThread();
        PacketDecodeEvent event = new PacketDecodeEvent(channelHandlerContext, playerData, byteBufWrapper, async);

        try {
            this.pluginManager.callEvent(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!event.isCancelled()) {
            list.add(bytebuf.readBytes(bytebuf.readableBytes()));
        } else {
            bytebuf.skipBytes(bytebuf.readableBytes());
        }
    }
}
