package io.github.retrooper.packetevents.packetwrappers.play.in.helditemslot;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;

public final class WrappedPacketInHeldItemSlot extends WrappedPacket {

    public WrappedPacketInHeldItemSlot(NMSPacket packet) {
        super(packet);
    }

    public int getCurrentSelectedSlot() {
        return readInt(0);
    }

    public void setCurrentSelectedSlot(int slot) {
        writeInt(0, slot);
    }
}
