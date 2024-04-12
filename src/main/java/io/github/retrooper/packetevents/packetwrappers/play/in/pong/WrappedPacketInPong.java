package io.github.retrooper.packetevents.packetwrappers.play.in.pong;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.server.ServerVersion;

public class WrappedPacketInPong extends WrappedPacket {

    private int id;

    public WrappedPacketInPong(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketInPong(int id) {
        this.id = id;
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
}
