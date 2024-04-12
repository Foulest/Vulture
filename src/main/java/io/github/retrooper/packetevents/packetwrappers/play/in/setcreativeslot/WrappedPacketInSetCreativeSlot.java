package io.github.retrooper.packetevents.packetwrappers.play.in.setcreativeslot;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
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
        return readItemStack(0);
    }

    public void setClickedItem(ItemStack stack) {
        writeItemStack(0, stack);
    }
}
