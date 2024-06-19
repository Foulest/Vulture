package io.github.retrooper.packetevents.packetwrappers.play.out.keepalive;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.reflection.Reflection;

import java.lang.reflect.Constructor;

public class WrappedPacketOutKeepAlive extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> keepAliveConstructor;
    private static boolean integerMode;
    private long id;

    public WrappedPacketOutKeepAlive(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutKeepAlive(long id) {
        this.id = id;
    }

    @Override
    protected void load() {
        Class<?> packetClass = PacketTypeClasses.Play.Server.KEEP_ALIVE;
        integerMode = Reflection.getField(packetClass, int.class, 0) != null;

        if (integerMode) {
            try {
                keepAliveConstructor = packetClass.getConstructor(int.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            try {
                keepAliveConstructor = packetClass.getConstructor(long.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public long getId() {
        if (packet != null) {
            if (integerMode) {
                return readInt(0);
            } else {
                return readLong(0);
            }
        } else {
            return id;
        }
    }

    public void setId(long id) throws UnsupportedOperationException {
        if (packet != null && integerMode && (id < Integer.MIN_VALUE || id > Integer.MAX_VALUE)) {
            throw new UnsupportedOperationException("PacketEvents failed to set the Keep Alive ID"
                    + " in WrappedPacketOutKeepAlive. Your server version does not support IDs outside"
                    + " the range of an int primitive type. Your Keep Alive ID seems to be in the range"
                    + " of a long primitive type.");
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        if (integerMode) {
            return keepAliveConstructor.newInstance((int) getId());
        } else {
            return keepAliveConstructor.newInstance(getId());
        }
    }
}
