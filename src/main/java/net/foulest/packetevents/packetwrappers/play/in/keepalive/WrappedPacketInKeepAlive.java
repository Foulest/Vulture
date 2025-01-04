package net.foulest.packetevents.packetwrappers.play.in.keepalive;

import net.foulest.packetevents.packettype.PacketTypeClasses;
import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;
import net.foulest.packetevents.utils.reflection.Reflection;

public final class WrappedPacketInKeepAlive extends WrappedPacket {

    private static boolean integerPresentInIndex0;

    public WrappedPacketInKeepAlive(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        Class<?> packetClass = PacketTypeClasses.Play.Client.KEEP_ALIVE;
        integerPresentInIndex0 = Reflection.getField(packetClass, int.class, 0) != null;
    }

    public long getId() {
        if (integerPresentInIndex0) {
            return readInt(0);
        } else {
            return readLong(0);
        }
    }

    public void setId(long id) {
        if (integerPresentInIndex0) {
            if (id > Integer.MAX_VALUE) {
                id = Integer.MAX_VALUE;
            } else if (id < Integer.MIN_VALUE) {
                id = Integer.MIN_VALUE;
            }

            writeInt(0, (int) id);
        } else {
            writeLong(0, id);
        }
    }
}
