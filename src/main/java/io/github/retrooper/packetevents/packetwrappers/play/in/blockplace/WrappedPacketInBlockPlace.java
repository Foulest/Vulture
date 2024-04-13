package io.github.retrooper.packetevents.packetwrappers.play.in.blockplace;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.player.Direction;
import io.github.retrooper.packetevents.utils.player.Hand;
import io.github.retrooper.packetevents.utils.vector.Vector3f;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class WrappedPacketInBlockPlace extends WrappedPacket {

    public WrappedPacketInBlockPlace(NMSPacket packet) {
        super(packet);
    }

    public Hand getHand() {
        return Hand.MAIN_HAND;
    }

    public Direction getDirection() {
        WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(new NMSPacket(packet.getRawNMSPacket()));
        return Direction.getDirection(blockPlace_1_8.getFace());
    }

    public void setDirection(@NotNull Direction direction) {
        WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(new NMSPacket(packet.getRawNMSPacket()));
        blockPlace_1_8.setFace(direction.getFaceValue());
    }

    public Vector3i getBlockPosition() {
        WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(packet);
        return blockPlace_1_8.getBlockPosition();
    }

    public @NotNull Optional<Vector3f> getCursorPosition() {
        WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(packet);
        return Optional.of(blockPlace_1_8.getCursorPosition());
    }

    public void setCursorPosition(Vector3f cursorPos) {
        WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(packet);
        blockPlace_1_8.setCursorPosition(cursorPos);
    }

    public @NotNull Optional<ItemStack> getItemStack() {
        WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(packet);
        return Optional.of(blockPlace_1_8.getItemStack());
    }

    public void setItemStack(ItemStack stack) {
        WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(packet);
        blockPlace_1_8.setItemStack(stack);
    }
}
