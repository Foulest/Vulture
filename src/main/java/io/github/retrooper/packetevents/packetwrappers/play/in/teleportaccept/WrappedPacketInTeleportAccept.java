package io.github.retrooper.packetevents.packetwrappers.play.in.teleportaccept;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;

class WrappedPacketInTeleportAccept extends WrappedPacket {

    WrappedPacketInTeleportAccept(NMSPacket packet) {
        super(packet);
    }

    public int getTeleportId() {
        return readInt(0);
    }

    public void setTeleportId(int teleportId) {
        writeInt(0, teleportId);
    }
}
