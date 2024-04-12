package io.github.retrooper.packetevents.packetwrappers.status.in.ping;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;

public class WrappedPacketStatusPing extends WrappedPacket {

    public WrappedPacketStatusPing(NMSPacket packet) {
        super(packet);
    }

    public long getPayload() {
        return readLong(0);
    }

    public void setPayload(long payload) {
        writeLong(0, payload);
    }
}
