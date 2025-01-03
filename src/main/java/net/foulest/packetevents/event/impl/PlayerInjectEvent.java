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

import lombok.Getter;
import net.foulest.packetevents.PacketEvents;
import net.foulest.packetevents.event.PacketEvent;
import net.foulest.packetevents.event.PacketListenerAbstract;
import net.foulest.packetevents.event.eventtypes.CancellableEvent;
import net.foulest.packetevents.event.eventtypes.PlayerEvent;
import net.foulest.packetevents.utils.netty.channel.ChannelUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

/**
 * The {@code PlayerInjectEvent} event is fired whenever a player is injected.
 * A player is injected by PacketEvents whenever they join the server.
 * This class implements {@link CancellableEvent} and {@link PlayerEvent}.
 *
 * @author retrooper
 * @see <a href="https://github.com/retrooper/packetevents/blob/dev/src/main/java/io/github/retrooper/packetevents/handler/PacketHandlerInternal.java">https://github.com/retrooper/packetevents/blob/dev/src/main/java/io/github/retrooper/packetevents/handler/PacketHandlerInternal.java</a>
 * @since 1.6.9
 */
@Getter
public final class PlayerInjectEvent extends PacketEvent implements CancellableEvent, PlayerEvent {

    private final Player player;
    private final InetSocketAddress socketAddress;
    private boolean cancelled;

    public PlayerInjectEvent(Player player) {
        this.player = player;
        socketAddress = ChannelUtils.getSocketAddress(PacketEvents.getInstance().getPlayerUtils().getChannel(player));
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean value) {
        cancelled = value;
    }

    /**
     * This method returns the bukkit player object of the player being injected.
     * This player might not be fully initialized.
     *
     * @return Injected Player.
     */
    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void call(@NotNull PacketListenerAbstract listener) {
        listener.onPlayerInject(this);
    }
}
