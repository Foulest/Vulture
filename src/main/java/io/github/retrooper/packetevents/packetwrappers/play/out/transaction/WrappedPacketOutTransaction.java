package io.github.retrooper.packetevents.packetwrappers.play.out.transaction;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ToString
@AllArgsConstructor
public class WrappedPacketOutTransaction extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private int windowID;
    private short actionNumber;
    private boolean accepted;

    public WrappedPacketOutTransaction(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        Class<?> packetClass = PacketTypeClasses.Play.Server.TRANSACTION;

        try {
            packetConstructor = packetClass.getConstructor(int.class, short.class, boolean.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    private int getWindowId() {
        if (nmsPacket != null) {
            return readInt(0);
        } else {
            return windowID;
        }
    }

    public void setWindowId(int windowID) {
        if (nmsPacket != null) {
            writeInt(0, windowID);
        } else {
            this.windowID = windowID;
        }
    }

    public short getActionNumber() {
        if (nmsPacket != null) {
            return readShort(0);
        } else {
            return actionNumber;
        }
    }

    public void setActionNumber(short actionNumber) {
        if (nmsPacket != null) {
            writeShort(0, actionNumber);
        } else {
            this.actionNumber = actionNumber;
        }
    }

    private boolean isAccepted() {
        if (nmsPacket != null) {
            return readBoolean(0);
        } else {
            return accepted;
        }
    }

    public void setAccepted(boolean isAccepted) {
        if (nmsPacket != null) {
            writeBoolean(0, isAccepted);
        } else {
            accepted = isAccepted;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return packetConstructor.newInstance(getWindowId(), getActionNumber(), isAccepted());
    }
}
