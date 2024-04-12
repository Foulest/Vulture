package io.github.retrooper.packetevents.packetwrappers.play.in.jigsawgenerate;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.vector.Vector3i;

/**
 * Sent when Generate is pressed on the Jigsaw Block interface.
 *
 * @author Tecnio
 */
public class WrappedPacketInJigsawGenerate extends WrappedPacket {

    public WrappedPacketInJigsawGenerate(NMSPacket packet) {
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
