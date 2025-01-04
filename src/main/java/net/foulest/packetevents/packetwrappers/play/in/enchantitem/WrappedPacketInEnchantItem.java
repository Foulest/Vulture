package net.foulest.packetevents.packetwrappers.play.in.enchantitem;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;

public class WrappedPacketInEnchantItem extends WrappedPacket {

    public WrappedPacketInEnchantItem(NMSPacket packet) {
        super(packet);
    }

    public int getWindowId() {
        return readInt(0);
    }

    public void setWindowId(int windowID) {
        writeInt(0, windowID);
    }

    public int getButton() {
        return readInt(1);
    }

    public void setButton(int button) {
        writeInt(1, button);
    }
}
