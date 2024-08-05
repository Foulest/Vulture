package io.github.retrooper.packetevents.packetwrappers.play.in.pickitem;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;

class WrappedPacketInPickItem extends WrappedPacket {

    WrappedPacketInPickItem(NMSPacket packet) {
        super(packet);
    }

    public int getSlot() {
        return readInt(0);
    }

    public void setSlot(int slot) {
        writeInt(0, slot);
    }
}
