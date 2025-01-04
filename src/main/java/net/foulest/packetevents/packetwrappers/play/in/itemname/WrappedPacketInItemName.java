package net.foulest.packetevents.packetwrappers.play.in.itemname;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;

class WrappedPacketInItemName extends WrappedPacket {

    WrappedPacketInItemName(NMSPacket packet) {
        super(packet);
    }

    public String getItemName() {
        return readString(0);
    }

    public void setItemName(String itemName) {
        writeString(0, itemName);
    }
}
