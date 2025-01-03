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

import net.foulest.packetevents.event.PacketListenerAbstract;
import net.foulest.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import net.foulest.packetevents.packetwrappers.NMSPacket;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code PacketLoginReceiveEvent} event is fired whenever the LOGIN packet is received from a client.
 * The {@code PacketLoginReceiveEvent} has no Bukkit player, the player object is null in this state.
 * Use the {@link #getSocketAddress()} to identify who sends the packet.
 *
 * @author retrooper
 * @see <a href="https://wiki.vg/Protocol#Login">https://wiki.vg/Protocol#Login</a>
 * @since 1.8
 */
public class PacketLoginReceiveEvent extends CancellableNMSPacketEvent {

    public PacketLoginReceiveEvent(Object channel, NMSPacket packet) {
        super(channel, packet);
    }

    @Override
    public void call(@NotNull PacketListenerAbstract listener) {
        if (listener.clientSidedLoginAllowance == null
                || listener.clientSidedLoginAllowance.contains(getPacketId())) {
            listener.onPacketLoginReceive(this);
        }
    }
}
