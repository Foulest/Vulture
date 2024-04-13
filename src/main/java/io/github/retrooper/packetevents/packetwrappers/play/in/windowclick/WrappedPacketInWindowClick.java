package io.github.retrooper.packetevents.packetwrappers.play.in.windowclick;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import org.bukkit.inventory.ItemStack;

public class WrappedPacketInWindowClick extends WrappedPacket {

    public WrappedPacketInWindowClick(NMSPacket packet) {
        super(packet);
    }

    // Unique ID for the inventory, 0 for player's inventory
    public int getWindowId() {
        return readInt(0);
    }

    public void setWindowId(int windowID) {
        writeInt(0, windowID);
    }

    // ID of clicked slot
    public int getWindowSlot() {
        return readInt(1);
    }

    public void setWindowSlot(int slot) {
        writeInt(1, slot);
    }

    // Left or right click
    public int getWindowButton() {
        return readInt(2);
    }

    public void setWindowButton(int button) {
        writeInt(2, button);
    }

    // Used to sync together client and server
    public int getActionNumber() {
        return readShort(0);
    }

    public void setActionNumber(int actionNumber) {
        writeShort(0, (short) actionNumber);
    }

    // Type of click - shift clicking, hotbar, drag, pickup...
    public int getMode() {
        return readInt(3);
    }

    public void setMode(int mode) {
        writeInt(3, mode);
    }

    /**
     * Get the clicked item.
     *
     * @return Get Clicked ItemStack
     */
    public ItemStack getClickedItemStack() {
        return readItemStack(0);
    }

    public void setClickedItemStack(ItemStack stack) {
        writeItemStack(0, stack);
    }
}
