package io.github.retrooper.packetevents.packetwrappers.status.out.pong;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;

import java.lang.reflect.Constructor;

public class WrappedPacketStatusPong extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private long payload;

    public WrappedPacketStatusPong(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketStatusPong(long payload) {
        this.payload = payload;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Status.Server.PONG.getConstructor(long.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public long getPayload() {
        if (packet != null) {
            return readLong(0);
        } else {
            return payload;
        }
    }

    public void setPayload(long payload) {
        if (packet != null) {
            writeLong(0, payload);
        } else {
            this.payload = payload;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        return packetConstructor.newInstance(getPayload());
    }
}
