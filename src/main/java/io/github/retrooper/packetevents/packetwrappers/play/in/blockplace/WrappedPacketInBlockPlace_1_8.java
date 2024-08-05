package io.github.retrooper.packetevents.packetwrappers.play.in.blockplace;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.vector.Vector3f;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

final class WrappedPacketInBlockPlace_1_8 extends WrappedPacket {

    WrappedPacketInBlockPlace_1_8(NMSPacket packet) {
        super(packet);
    }

    Vector3i getBlockPosition() {
        return readBlockPosition(1);
    }

    public void setBlockPosition(Vector3i blockPos) {
        writeBlockPosition(1, blockPos);
    }

    ItemStack getItemStack() {
        return readItemStack();
    }

    void setItemStack(ItemStack stack) {
        writeItemStack(stack);
    }

    int getFace() {
        return readInt(0);
    }

    void setFace(int face) {
        writeInt(0, face);
    }

    @Contract(" -> new")
    @NotNull
    Vector3f getCursorPosition() {
        return new Vector3f(readFloat(0), readFloat(1), readFloat(2));
    }

    void setCursorPosition(@NotNull Vector3f cursorPos) {
        writeFloat(0, cursorPos.x);
        writeFloat(1, cursorPos.y);
        writeFloat(2, cursorPos.z);
    }
}
