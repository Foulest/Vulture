package io.github.retrooper.packetevents.packetwrappers.play.in.transaction;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;

public final class WrappedPacketInTransaction extends WrappedPacket {

    public WrappedPacketInTransaction(NMSPacket packet) {
        super(packet);
    }

    public int getWindowId() {
        return readInt(0);
    }

    public void setWindowId(int windowID) {
        writeInt(0, windowID);
    }

    public short getActionNumber() {
        return readShort(0);
    }

    public void setActionNumber(short actionNumber) {
        writeShort(0, actionNumber);
    }

    public boolean isAccepted() {
        return readBoolean(0);
    }

    public void setAccepted(boolean isAccepted) {
        writeBoolean(0, isAccepted);
    }
}
