package net.foulest.packetevents.packetwrappers.play.in.tabcomplete;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;

public class WrappedPacketInTabComplete extends WrappedPacket {

    public WrappedPacketInTabComplete(NMSPacket packet) {
        super(packet);
    }

    public String getText() {
        return readString(0);
    }

    public void setText(String text) {
        writeString(0, text);
    }
}
