package net.foulest.packetevents.packetwrappers.play.in.blockplace;

import lombok.ToString;
import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;
import net.foulest.packetevents.packetwrappers.api.WrapperPacketReader;
import net.foulest.packetevents.packetwrappers.api.WrapperPacketWriter;
import net.foulest.packetevents.utils.enums.EnumUtil;
import net.foulest.packetevents.utils.nms.NMSUtils;
import net.foulest.packetevents.utils.player.Direction;
import net.foulest.packetevents.utils.vector.Vector3i;
import org.jetbrains.annotations.NotNull;

@ToString
final class WrappedPacketInBlockPlace_1_9 extends WrappedPacket {

    private Object movingObjPos;

    WrappedPacketInBlockPlace_1_9(NMSPacket packet) {
        super(packet);
    }

    public Vector3i getBlockPosition() {
        if (NMSUtils.movingObjectPositionBlockClass == null) {
            return readBlockPosition(0);
        } else {
            if (movingObjPos == null) {
                movingObjPos = readObject(0, NMSUtils.movingObjectPositionBlockClass);
            }

            WrappedPacket movingObjectPosWrapper = new WrappedPacket(new NMSPacket(movingObjPos));
            return movingObjectPosWrapper.readBlockPosition(0);
        }
    }

    public void setBlockPosition(Vector3i blockPos) {
        if (NMSUtils.movingObjectPositionBlockClass == null) {
            writeBlockPosition(0, blockPos);
        } else {
            if (movingObjPos == null) {
                movingObjPos = readObject(0, NMSUtils.movingObjectPositionBlockClass);
            }

            WrappedPacket movingObjectPosWrapper = new WrappedPacket(new NMSPacket(movingObjPos));
            movingObjectPosWrapper.writeBlockPosition(0, blockPos);
        }
    }

    public Direction getDirection() {
        Enum<?> enumConst;

        if (NMSUtils.movingObjectPositionBlockClass == null) {
            enumConst = readEnumConstant(0, NMSUtils.enumDirectionClass);
        } else {
            if (movingObjPos == null) {
                movingObjPos = readObject(0, NMSUtils.movingObjectPositionBlockClass);
            }

            WrapperPacketReader movingObjectPosWrapper = new WrappedPacket(new NMSPacket(movingObjPos));
            enumConst = movingObjectPosWrapper.readEnumConstant(0, NMSUtils.enumDirectionClass);
        }
        return Direction.values()[enumConst.ordinal()];
    }

    public void setDirection(@NotNull Direction direction) {
        Enum<?> enumConst = EnumUtil.valueByIndex(NMSUtils.enumDirectionClass.asSubclass(Enum.class), direction.ordinal());

        if (NMSUtils.movingObjectPositionBlockClass == null) {
            writeEnumConstant(0, enumConst);
        } else {
            if (movingObjPos == null) {
                movingObjPos = readObject(0, NMSUtils.movingObjectPositionBlockClass);
            }

            WrapperPacketWriter movingObjectPosWrapper = new WrappedPacket(new NMSPacket(movingObjPos));
            movingObjectPosWrapper.writeEnumConstant(0, enumConst);
        }
    }
}
