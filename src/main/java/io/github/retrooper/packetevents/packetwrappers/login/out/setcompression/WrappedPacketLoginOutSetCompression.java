package io.github.retrooper.packetevents.packetwrappers.login.out.setcompression;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import lombok.AllArgsConstructor;

import java.lang.reflect.Constructor;

@AllArgsConstructor
public class WrappedPacketLoginOutSetCompression extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> constructor;
    private int threshold;

    public WrappedPacketLoginOutSetCompression(NMSPacket packet) {
        super(packet);
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
}
