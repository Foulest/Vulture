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
package net.foulest.packetevents.event.eventtypes;

import lombok.Getter;
import lombok.ToString;
import net.foulest.packetevents.event.PacketEvent;
import net.foulest.packetevents.packettype.PacketType;
import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.utils.netty.channel.ChannelUtils;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

/**
 * The {@code NMSPacketEvent} abstract class represents an event that has to do with an actual packet.
 * Don't mix this up with {@link PacketEvent}.
 * The PacketEvent class represents an event that belongs to PacketEvent's packet system.
 *
 * @author retrooper
 * @since 1.8
 */
@Getter
@ToString
public abstract class NMSPacketEvent extends PacketEvent {

    private final Object channel;
    private final InetSocketAddress socketAddress;
    private final byte packetId;
    protected NMSPacket nmsPacket;

    protected NMSPacketEvent(Object channel, @NotNull NMSPacket nmsPacket) {
        this.channel = channel;
        socketAddress = ChannelUtils.getSocketAddress(channel);
        this.nmsPacket = nmsPacket;
        packetId = PacketType.getPacketIDMap().getOrDefault(nmsPacket.getRawNMSPacket().getClass(), PacketType.INVALID);
    }

    /**
     * Get the NMS packet.
     *
     * @return Get NMS packet.
     */
    public NMSPacket getNMSPacket() {
        return nmsPacket;
    }

    /**
     * Update the NMS Packet.
     *
     * @param nmsPacket NMS Object
     */
    public void setNMSPacket(NMSPacket nmsPacket) {
        this.nmsPacket = nmsPacket;
    }
}
