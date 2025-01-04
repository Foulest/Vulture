package net.foulest.packetevents.packetwrappers.play.in.jigsawgenerate;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;
import net.foulest.packetevents.utils.vector.Vector3i;

/**
 * Sent when Generate is pressed on the Jigsaw Block interface.
 *
 * @author Tecnio
 */
class WrappedPacketInJigsawGenerate extends WrappedPacket {

    WrappedPacketInJigsawGenerate(NMSPacket packet) {
        super(packet);
    }

    public Vector3i getBlockPosition() {
        return readBlockPosition(0);
    }

    public void setBlockPosition(Vector3i blockPosition) {
        writeBlockPosition(0, blockPosition);
    }

    public int getLevels() {
        return readInt(0);
    }

    public void setLevels(int levels) {
        writeInt(0, levels);
    }

    public boolean isKeepingJigsaws() {
        return readBoolean(0);
    }

    public void setKeepingJigsaws(boolean keepingJigsaws) {
        writeBoolean(0, keepingJigsaws);
    }
}
