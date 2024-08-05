package io.github.retrooper.packetevents.packetwrappers.play.out.closewindow;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ToString
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
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    private int getWindowId() {
        if (nmsPacket != null) {
            return readInt(0);
        }
        return windowID;
    }

    public void setWindowId(int windowID) {
        if (nmsPacket != null) {
            writeInt(0, windowID);
        } else {
            this.windowID = windowID;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return constructor.newInstance(getWindowId());
    }
}
