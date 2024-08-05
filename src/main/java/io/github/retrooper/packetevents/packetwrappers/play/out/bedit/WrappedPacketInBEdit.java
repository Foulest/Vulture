package io.github.retrooper.packetevents.packetwrappers.play.out.bedit;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import org.bukkit.inventory.ItemStack;

class WrappedPacketInBEdit extends WrappedPacket {

    WrappedPacketInBEdit(NMSPacket packet) {
        super(packet);
    }

    public ItemStack getItemStack() {
        return readItemStack();
    }

    public void setItemStack(ItemStack itemStack) {
        writeItemStack(itemStack);
    }

    public boolean isSigning() {
        return readBoolean(0);
    }

    public void setSigning(boolean signing) {
        writeBoolean(0, signing);
    }
}
