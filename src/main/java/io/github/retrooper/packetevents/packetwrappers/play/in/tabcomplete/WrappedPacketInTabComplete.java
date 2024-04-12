package io.github.retrooper.packetevents.packetwrappers.play.in.tabcomplete;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;

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
