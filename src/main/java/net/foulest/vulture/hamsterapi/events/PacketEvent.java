package net.foulest.vulture.hamsterapi.events;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.data.PlayerData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class PacketEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final ChannelHandlerContext channelHandlerContext;
    private final PlayerData playerData;
    @Setter
    private boolean cancelled = false;

    public PacketEvent(ChannelHandlerContext channelHandlerContext, PlayerData playerData, boolean async) {
        super(async);
        this.channelHandlerContext = channelHandlerContext;
        this.playerData = playerData;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
