package net.foulest.packetevents.packetwrappers.play.in.trsel;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;

class WrappedPacketInTrSel extends WrappedPacket {

    WrappedPacketInTrSel(NMSPacket packet) {
        super(packet);
    }

    public int getSlot() {
        return readInt(0);
    }

    public void setSlot(int slot) {
        writeInt(0, slot);
    }
}
