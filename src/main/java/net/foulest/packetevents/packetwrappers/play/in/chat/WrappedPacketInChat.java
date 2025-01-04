package net.foulest.packetevents.packetwrappers.play.in.chat;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;

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
