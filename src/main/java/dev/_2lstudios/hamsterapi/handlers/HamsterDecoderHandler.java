package dev._2lstudios.hamsterapi.handlers;

import dev._2lstudios.hamsterapi.events.PacketDecodeEvent;
import dev._2lstudios.hamsterapi.wrappers.ByteBufWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.MessageUtil;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HamsterDecoderHandler extends ByteToMessageDecoder {

    private final Server server;
    private final PluginManager pluginManager;
    private final PlayerData playerData;

    public HamsterDecoderHandler(@NotNull PlayerData playerData) {
        server = playerData.getPlayer().getServer();
        pluginManager = server.getPluginManager();
        this.playerData = playerData;
    }

    /**
     * Called when a packet is received from the client.
     *
     * @param channelHandlerContext The channel handler context.
     * @param byteBuf               The byte buffer.
     * @param list                  The list of objects.
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        ByteBufWrapper byteBufWrapper = new ByteBufWrapper(byteBuf);
        boolean async = !server.isPrimaryThread();
        PacketDecodeEvent event = new PacketDecodeEvent(channelHandlerContext, playerData, byteBufWrapper, async);

        try {
            pluginManager.callEvent(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!event.isCancelled()) {
            list.add(byteBuf.readBytes(byteBuf.readableBytes()));
        } else {
            byteBuf.skipBytes(byteBuf.readableBytes());
        }
    }
}
