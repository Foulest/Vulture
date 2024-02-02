package dev._2lstudios.hamsterapi.handlers;

import dev._2lstudios.hamsterapi.events.PacketReceiveEvent;
import dev._2lstudios.hamsterapi.events.PacketSendEvent;
import dev._2lstudios.hamsterapi.wrappers.PacketWrapper;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.MessageUtil;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public class HamsterChannelHandler extends ChannelDuplexHandler {

    private final Server server;
    private final PluginManager pluginManager;
    private final PlayerData playerData;

    public HamsterChannelHandler(@NotNull PlayerData playerData) {
        server = playerData.getPlayer().getServer();
        pluginManager = server.getPluginManager();
        this.playerData = playerData;
    }

    /**
     * Called when a packet is sent to the client.
     *
     * @param channelHandlerContext The channel handler context.
     * @param packet                The packet sent.
     * @param channelPromise        The channel promise.
     * @throws Exception If an error occurs.
     */
    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object packet,
                      ChannelPromise channelPromise) throws Exception {
        PacketWrapper packetWrapper = new PacketWrapper(packet);
        boolean async = !server.isPrimaryThread();
        PacketSendEvent event = new PacketSendEvent(channelHandlerContext, playerData, packetWrapper, async);

        try {
            pluginManager.callEvent(event);
        } catch (Exception ex) {
            MessageUtil.printException(ex);
        }

        if (!event.isCancelled()) {
            super.write(channelHandlerContext, packetWrapper.getPacket(), channelPromise);
        }
    }

    /**
     * Called when a packet is received from the client.
     *
     * @param channelHandlerContext The channel handler context.
     * @param packet                The packet received.
     * @throws Exception If an error occurs.
     */
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
        PacketWrapper packetWrapper = new PacketWrapper(packet);
        boolean async = !server.isPrimaryThread();
        PacketReceiveEvent event = new PacketReceiveEvent(channelHandlerContext, playerData, packetWrapper, async);

        try {
            pluginManager.callEvent(event);
        } catch (Exception ex) {
            MessageUtil.printException(ex);
        }

        if (!event.isCancelled()) {
            super.channelRead(channelHandlerContext, packetWrapper.getPacket());
        }
    }
}
