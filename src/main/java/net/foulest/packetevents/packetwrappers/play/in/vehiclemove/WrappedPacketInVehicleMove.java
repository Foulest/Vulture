package net.foulest.packetevents.packetwrappers.play.in.vehiclemove;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;
import net.foulest.packetevents.utils.vector.Vector3d;
import org.jetbrains.annotations.NotNull;

class WrappedPacketInVehicleMove extends WrappedPacket {

    WrappedPacketInVehicleMove(NMSPacket packet) {
        super(packet);
    }

    public Vector3d getPosition() {
        return new Vector3d(readDouble(0), readDouble(1), readDouble(2));
    }

    public void setPosition(@NotNull Vector3d position) {
        writeDouble(0, position.x);
        writeDouble(1, position.y);
        writeDouble(2, position.z);
    }

    public float getYaw() {
        return readFloat(0);
    }

    public void setYaw(float yaw) {
        writeFloat(0, yaw);
    }

    public float getPitch() {
        return readFloat(1);
    }

    public void setPitch(float pitch) {
        writeFloat(1, pitch);
    }
}