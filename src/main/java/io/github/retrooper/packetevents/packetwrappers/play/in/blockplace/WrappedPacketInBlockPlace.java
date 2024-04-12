package io.github.retrooper.packetevents.packetwrappers.play.in.blockplace;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.player.Direction;
import io.github.retrooper.packetevents.utils.player.Hand;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import io.github.retrooper.packetevents.utils.vector.Vector3f;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;


public final class WrappedPacketInBlockPlace extends WrappedPacket {

    private static boolean newerThan_v_1_8_8;
    private static boolean newerThan_v_1_7_10;
    private static int handEnumIndex;

    public WrappedPacketInBlockPlace(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        newerThan_v_1_7_10 = version.isNewerThan(ServerVersion.v_1_7_10);
        newerThan_v_1_8_8 = version.isNewerThan(ServerVersion.v_1_8_8);

        try {
            Object handEnum = readObject(1, NMSUtils.enumHandClass);
            handEnumIndex = 1;
        } catch (Exception ex) {
            handEnumIndex = 0;//Most likely a newer version
        }
    }

    public Hand getHand() {
        if (newerThan_v_1_8_8) {
            Enum<?> enumConst = readEnumConstant(handEnumIndex, NMSUtils.enumHandClass);
            return Hand.values()[enumConst.ordinal()];
        } else {
            return Hand.MAIN_HAND;
        }
    }

    public void setHand(Hand hand) {
        // Optimize to do nothing on legacy versions.
        // The protocol of the legacy versions only support one hand, the main hand.
        if (newerThan_v_1_8_8) {
            Enum<?> enumConst = EnumUtil.valueByIndex(NMSUtils.enumHandClass, hand.ordinal());
            writeEnumConstant(handEnumIndex, enumConst);
        }
    }

    public Direction getDirection() {
        if (newerThan_v_1_8_8) {
            WrappedPacketInBlockPlace_1_9 blockPlace_1_9 = new WrappedPacketInBlockPlace_1_9(new NMSPacket(packet.getRawNMSPacket()));
            return blockPlace_1_9.getDirection();
        } else {
            WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(new NMSPacket(packet.getRawNMSPacket()));
            return Direction.getDirection(blockPlace_1_8.getFace());
        }
    }

    public void setDirection(Direction direction) {
        if (newerThan_v_1_8_8) {
            WrappedPacketInBlockPlace_1_9 blockPlace_1_9 = new WrappedPacketInBlockPlace_1_9(new NMSPacket(packet.getRawNMSPacket()));
            blockPlace_1_9.setDirection(direction);
        } else if (newerThan_v_1_7_10) {
            WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(new NMSPacket(packet.getRawNMSPacket()));
            blockPlace_1_8.setFace(direction.getFaceValue());
        }
    }

    public Vector3i getBlockPosition() {
        Vector3i blockPos;

        if (newerThan_v_1_8_8) {
            WrappedPacketInBlockPlace_1_9 blockPlace_1_9 = new WrappedPacketInBlockPlace_1_9(packet);
            blockPos = blockPlace_1_9.getBlockPosition();
        } else {
            WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(packet);
            blockPos = blockPlace_1_8.getBlockPosition();
        }
        return blockPos;
    }

    public Optional<Vector3f> getCursorPosition() {
        if (newerThan_v_1_8_8) {
            return Optional.empty();
        } else {
            WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(packet);
            return Optional.of(blockPlace_1_8.getCursorPosition());
        }
    }

    public void setCursorPosition(Vector3f cursorPos) {
        if (!newerThan_v_1_8_8 && newerThan_v_1_7_10) {
            WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(packet);
            blockPlace_1_8.setCursorPosition(cursorPos);
        }
    }

    public Optional<ItemStack> getItemStack() {
        if (newerThan_v_1_8_8) {
            return Optional.empty();
        } else {
            WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(packet);
            return Optional.of(blockPlace_1_8.getItemStack());
        }
    }

    public void setItemStack(ItemStack stack) {
        if (!newerThan_v_1_8_8 && newerThan_v_1_7_10) {
            WrappedPacketInBlockPlace_1_8 blockPlace_1_8 = new WrappedPacketInBlockPlace_1_8(packet);
            blockPlace_1_8.setItemStack(stack);
        }
    }
}
