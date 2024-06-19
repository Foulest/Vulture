package io.github.retrooper.packetevents.packetwrappers.play.in.blockdig;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.player.Direction;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import org.jetbrains.annotations.NotNull;

public final class WrappedPacketInBlockDig extends WrappedPacket {

    private static Class<? extends Enum<?>> digTypeClass;

    public WrappedPacketInBlockDig(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        Class<?> blockDigClass = PacketTypeClasses.Play.Client.BLOCK_DIG;

        try {
            digTypeClass = NMSUtils.getNMSEnumClass("EnumPlayerDigType");
        } catch (ClassNotFoundException e) {
            // It is probably a subclass
            digTypeClass = SubclassUtil.getEnumSubClass(blockDigClass, "EnumPlayerDigType");
        }
    }

    public Vector3i getBlockPosition() {
        return readBlockPosition(0);
    }

    public void setBlockPosition(Vector3i blockPos) {
        writeBlockPosition(0, blockPos);
    }

    /**
     * Get the direction / Get the face.
     *
     * @return Direction
     */
    public Direction getDirection() {
        Enum<?> enumDir = readEnumConstant(0, NMSUtils.enumDirectionClass);
        return Direction.values()[enumDir.ordinal()];
    }

    public void setDirection(@NotNull Direction direction) {
        Enum<?> enumConst = EnumUtil.valueByIndex(NMSUtils.enumDirectionClass.asSubclass(Enum.class), direction.ordinal());
        write(NMSUtils.enumDirectionClass, 0, enumConst);
    }

    /**
     * Get the PlayerDigType enum sent in this packet.
     *
     * @return Dig Type
     */
    public PlayerDigType getDigType() {
        return PlayerDigType.values()[readEnumConstant(0, digTypeClass).ordinal()];
    }

    public void setDigType(@NotNull PlayerDigType type) {
        Enum<?> enumConst = EnumUtil.valueByIndex(digTypeClass.asSubclass(Enum.class), type.ordinal());
        writeEnumConstant(0, enumConst);
    }

    public enum PlayerDigType {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_ITEM_WITH_OFFHAND
    }
}
