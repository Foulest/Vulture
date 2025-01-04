package net.foulest.packetevents.packetwrappers.play.in.setcreativeslot;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;
import org.bukkit.inventory.ItemStack;

public class WrappedPacketInSetCreativeSlot extends WrappedPacket {

    public WrappedPacketInSetCreativeSlot(NMSPacket packet) {
        super(packet);
    }

    public int getSlot() {
        return readInt(0);
    }

    public void setSlot(int value) {
        writeInt(0, value);
    }

    public ItemStack getClickedItem() {
        return readItemStack();
    }

    public void setClickedItem(ItemStack stack) {
        writeItemStack(stack);
    }
}
