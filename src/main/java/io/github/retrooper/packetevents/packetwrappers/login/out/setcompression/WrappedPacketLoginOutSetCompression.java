package io.github.retrooper.packetevents.packetwrappers.login.out.setcompression;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.server.ServerVersion;

import java.lang.reflect.Constructor;

public class WrappedPacketLoginOutSetCompression extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> constructor;
    private int threshold;

    public WrappedPacketLoginOutSetCompression(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketLoginOutSetCompression(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void load() {
        try {
            if (PacketTypeClasses.Login.Server.SET_COMPRESSION != null) {
                constructor = PacketTypeClasses.Login.Server.SET_COMPRESSION.getConstructor(int.class);
            }
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Maximum size of a packet before it can be compressed.
     *
     * @return threshold Threshold
     */
    public int getThreshold() {
        if (packet != null) {
            return readInt(0);
        }
        return threshold;
    }

    public void setThreshold(int threshold) {
        if (packet != null) {
            writeInt(0, threshold);
        } else {
            this.threshold = threshold;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        return constructor.newInstance(getThreshold());
    }

    @Override
    public boolean isSupported() {
        return version.isNewerThan(ServerVersion.v_1_7_10);
    }
}
