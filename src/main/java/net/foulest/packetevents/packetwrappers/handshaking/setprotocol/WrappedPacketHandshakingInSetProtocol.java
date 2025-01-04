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
package net.foulest.packetevents.packetwrappers.handshaking.setprotocol;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;

public class WrappedPacketHandshakingInSetProtocol extends WrappedPacket {

    public WrappedPacketHandshakingInSetProtocol(NMSPacket packet) {
        super(packet);
    }

    public int getProtocolVersion() {
        return readInt(0);
    }

    public void setProtocolVersion(int protocolVersion) {
        writeInt(0, protocolVersion);
    }

    public int getPort() {
        return readInt(1);
    }

    public void setPort(int port) {
        writeInt(1, port);
    }

    public String getHostName() {
        return readString(0);
    }

    public void setHostName(String hostName) {
        writeString(0, hostName);
    }
}
