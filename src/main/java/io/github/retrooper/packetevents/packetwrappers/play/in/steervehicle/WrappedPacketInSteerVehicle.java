package io.github.retrooper.packetevents.packetwrappers.play.in.steervehicle;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;

public class WrappedPacketInSteerVehicle extends WrappedPacket {

    public WrappedPacketInSteerVehicle(NMSPacket packet) {
        super(packet);
    }

    public float getSideValue() {
        return readFloat(0);
    }

    public void setSideValue(float value) {
        writeFloat(0, value);
    }

    public float getForwardValue() {
        return readFloat(1);
    }

    public void setForwardValue(float value) {
        writeFloat(1, value);
    }

    public boolean isJump() {
        return readBoolean(0);
    }

    public void setJump(boolean isJump) {
        writeBoolean(0, isJump);
    }

    public boolean isDismount() {
        return readBoolean(1);
    }

    public void setDismount(boolean isDismount) {
        writeBoolean(1, isDismount);
    }
}
