package net.foulest.packetevents.packetwrappers.play.in.teleportaccept;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;

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
