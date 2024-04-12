package io.github.retrooper.packetevents.packetwrappers.play.in.chat;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;

public final class WrappedPacketInChat extends WrappedPacket {

    public WrappedPacketInChat(NMSPacket packet) {
        super(packet);
    }

    public String getMessage() {
        return readString(0);
    }

    public void setMessage(String message) {
        writeString(0, message);
    }
}
