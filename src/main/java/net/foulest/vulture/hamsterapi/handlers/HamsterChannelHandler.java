package net.foulest.vulture.hamsterapi.handlers;

import net.foulest.vulture.hamsterapi.wrappers.PacketWrapper;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.hamsterapi.events.PacketReceiveEvent;
import net.foulest.vulture.hamsterapi.events.PacketSendEvent;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

public class HamsterChannelHandler extends ChannelDuplexHandler {

    private final Server server;
    private final PluginManager pluginManager;
    private final PlayerData playerData;

    public HamsterChannelHandler(PlayerData playerData) {
        this.server = playerData.getPlayer().getServer();
        this.pluginManager = server.getPluginManager();
        this.playerData = playerData;
    }

    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object packet,
                      ChannelPromise channelPromise) throws Exception {
        PacketWrapper packetWrapper = new PacketWrapper(packet);
        boolean async = !server.isPrimaryThread();
        PacketSendEvent event = new PacketSendEvent(channelHandlerContext, playerData, packetWrapper, async);

        try {
            this.pluginManager.callEvent(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!event.isCancelled()) {
            super.write(channelHandlerContext, packetWrapper.getPacket(), channelPromise);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
        PacketWrapper packetWrapper = new PacketWrapper(packet);
        boolean async = !server.isPrimaryThread();
        PacketReceiveEvent event = new PacketReceiveEvent(channelHandlerContext, playerData, packetWrapper, async);

        try {
            this.pluginManager.callEvent(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!event.isCancelled()) {
            super.channelRead(channelHandlerContext, packetWrapper.getPacket());
        }
    }
}
