package io.github.retrooper.packetevents.packetwrappers.play.out.ping;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.server.ServerVersion;

import java.lang.reflect.Constructor;

public class WrappedPacketOutPing extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private int id;

    public WrappedPacketOutPing(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutPing(int id) {
        this.id = id;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.PING.getConstructor(int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        if (packet != null) {
            return readInt(0);
        } else {
            return id;
        }
    }

    public void setId(int id) {
        if (packet != null) {
            writeInt(0, id);
        } else {
            this.id = id;
        }
    }

    @Override
    public boolean isSupported() {
        return version.isNewerThanOrEquals(ServerVersion.v_1_17);
    }

    @Override
    public Object asNMSPacket() throws Exception {
        return packetConstructor.newInstance(getId());
    }
}
