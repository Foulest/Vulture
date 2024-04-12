package io.github.retrooper.packetevents.packetwrappers.play.in.closewindow;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;

public class WrappedPacketInCloseWindow extends WrappedPacket {

    public WrappedPacketInCloseWindow(NMSPacket packet) {
        super(packet);
    }

    // 0 for player inv.
    public int getWindowId() {
        return readInt(0);
    }

    public void setWindowId(int windowID) {
        writeInt(0, windowID);
    }
}
