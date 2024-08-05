package io.github.retrooper.packetevents.packetwrappers.play.in.beacon;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;

class WrappedPacketInBeacon extends WrappedPacket {

    WrappedPacketInBeacon(NMSPacket packet) {
        super(packet);
    }

    public int getPrimaryEffect() {
        return readInt(0);
    }

    public void setPrimaryEffect(int primaryEffect) {
        writeInt(0, primaryEffect);
    }

    public int getSecondaryEffect() {
        return readInt(1);
    }

    public void setSecondaryEffect(int secondaryEffect) {
        writeInt(1, secondaryEffect);
    }
}
