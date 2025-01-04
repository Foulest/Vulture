package net.foulest.packetevents.packetwrappers.play.in.closewindow;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;

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
