package net.foulest.packetevents.packetwrappers.status.in.ping;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;

class WrappedPacketStatusPing extends WrappedPacket {

    WrappedPacketStatusPing(NMSPacket packet) {
        super(packet);
    }

    public long getPayload() {
        return readLong(0);
    }

    public void setPayload(long payload) {
        writeLong(0, payload);
    }
}
