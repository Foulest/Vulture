package io.github.retrooper.packetevents.packetwrappers.handshaking.setprotocol;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;

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
