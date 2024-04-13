package io.github.retrooper.packetevents.packetwrappers.play.out.bedit;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import org.bukkit.inventory.ItemStack;

public class WrappedPacketInBEdit extends WrappedPacket {

    public WrappedPacketInBEdit(NMSPacket packet) {
        super(packet);
    }

    public ItemStack getItemStack() {
        return readItemStack(0);
    }

    public void setItemStack(ItemStack itemStack) {
        writeItemStack(0, itemStack);
    }

    public boolean isSigning() {
        return readBoolean(0);
    }

    public void setSigning(boolean signing) {
        writeBoolean(0, signing);
    }
}
