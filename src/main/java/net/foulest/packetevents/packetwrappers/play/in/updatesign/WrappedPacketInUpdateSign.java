package net.foulest.packetevents.packetwrappers.play.in.updatesign;

import net.foulest.packetevents.packettype.PacketTypeClasses;
import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;
import net.foulest.packetevents.utils.nms.NMSUtils;
import net.foulest.packetevents.utils.reflection.Reflection;
import net.foulest.packetevents.utils.vector.Vector3i;

public class WrappedPacketInUpdateSign extends WrappedPacket {

    private static boolean v_1_7_mode;
    private static boolean strArrayMode;

    public WrappedPacketInUpdateSign(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        v_1_7_mode = Reflection.getField(PacketTypeClasses.Play.Client.UPDATE_SIGN, int.class, 0) != null;
        strArrayMode = Reflection.getField(PacketTypeClasses.Play.Client.UPDATE_SIGN, String[].class, 0) != null;
    }

    public Vector3i getBlockPosition() {
        if (v_1_7_mode) {
            int x = readInt(0);
            int y = readInt(1);
            int z = readInt(2);
            return new Vector3i(x, y, z);
        } else {
            return readBlockPosition(0);
        }
    }

    public void setBlockPosition(Vector3i blockPos) {
        if (v_1_7_mode) {
            writeInt(0, blockPos.x);
            writeInt(1, blockPos.y);
            writeInt(2, blockPos.z);
        } else {
            writeBlockPosition(0, blockPos);
        }
    }

    public String[] getTextLines() {
        if (strArrayMode) {
            // 1.7.10 and 1.17+
            return readStringArray(0);
        } else {
            // 1.8 -> 1.16.5
            Object[] iChatComponents = (Object[]) readAnyObject(1);
            return NMSUtils.readIChatBaseComponents(iChatComponents);
        }
    }

    public void setTextLines(String[] lines) {
        if (strArrayMode) {
            writeStringArray(0, lines);
        } else {
            Object[] iChatComponents = NMSUtils.generateIChatBaseComponents(lines);
            writeAnyObject(1, iChatComponents);
        }
    }
}
