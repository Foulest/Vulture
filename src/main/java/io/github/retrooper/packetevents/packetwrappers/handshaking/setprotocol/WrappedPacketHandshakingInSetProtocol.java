package io.github.retrooper.packetevents.packetwrappers.handshaking.setprotocol;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.server.ServerVersion;

public class WrappedPacketHandshakingInSetProtocol extends WrappedPacket {

    private static boolean v_1_17;

    public WrappedPacketHandshakingInSetProtocol(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        v_1_17 = version.isNewerThanOrEquals(ServerVersion.v_1_17);
    }

    public int getProtocolVersion() {
        return readInt(v_1_17 ? 1 : 0);
    }

    public void setProtocolVersion(int protocolVersion) {
        writeInt(v_1_17 ? 1 : 0, protocolVersion);
    }

    public int getPort() {
        return readInt(v_1_17 ? 2 : 1);
    }

    public void setPort(int port) {
        writeInt(v_1_17 ? 2 : 1, port);
    }

    public String getHostName() {
        return readString(0);
    }

    public void setHostName(String hostName) {
        writeString(0, hostName);
    }
}
