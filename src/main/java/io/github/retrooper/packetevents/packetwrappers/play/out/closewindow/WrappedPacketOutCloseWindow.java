package io.github.retrooper.packetevents.packetwrappers.play.out.closewindow;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;

import java.lang.reflect.Constructor;

public class WrappedPacketOutCloseWindow extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> constructor;
    private int windowID;

    public WrappedPacketOutCloseWindow(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutCloseWindow(int windowID) {
        this.windowID = windowID;
    }

    @Override
    protected void load() {
        try {
            constructor = PacketTypeClasses.Play.Server.CLOSE_WINDOW.getConstructor(int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public int getWindowId() {
        if (packet != null) {
            return readInt(0);
        }
        return windowID;
    }

    public void setWindowId(int windowID) {
        if (packet != null) {
            writeInt(0, windowID);
        } else {
            this.windowID = windowID;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        return constructor.newInstance(getWindowId());
    }
}
