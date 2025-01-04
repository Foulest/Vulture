/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.packetevents.event.impl;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.ToString;
import net.foulest.packetevents.event.PacketEvent;
import net.foulest.packetevents.event.PacketListenerAbstract;
import net.foulest.packetevents.event.eventtypes.CancellableEvent;
import net.foulest.packetevents.event.eventtypes.PlayerEvent;
import net.foulest.packetevents.utils.bytebuf.ByteBufWrapper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString
public class PacketDecodeEvent extends PacketEvent implements CancellableEvent, PlayerEvent {

    private final ChannelHandlerContext channelHandlerContext;
    private final Player player;
    private final ByteBufWrapper byteBuf;
    private final boolean async;
    private boolean cancelled;

    public PacketDecodeEvent(ChannelHandlerContext channelHandlerContext, Player player,
                             ByteBufWrapper byteBuf, boolean async) {
        this.channelHandlerContext = channelHandlerContext;
        this.player = player;
        this.byteBuf = byteBuf;
        this.async = async;
    }

    @Override
    public void call(@NotNull PacketListenerAbstract listener) {
        listener.onPacketDecode(this);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean val) {
        cancelled = val;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
