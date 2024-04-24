package io.github.retrooper.packetevents.packetwrappers.play.out.transaction;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import lombok.AllArgsConstructor;

import java.lang.reflect.Constructor;

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

    public int getWindowId() {
        if (packet != null) {
            return readInt(0);
        } else {
            return windowID;
        }
    }

    public void setWindowId(int windowID) {
        if (packet != null) {
            writeInt(0, windowID);
        } else {
            this.windowID = windowID;
        }
    }

    public short getActionNumber() {
        if (packet != null) {
            return readShort(0);
        } else {
            return actionNumber;
        }
    }

    public void setActionNumber(short actionNumber) {
        if (packet != null) {
            writeShort(0, actionNumber);
        } else {
            this.actionNumber = actionNumber;
        }
    }

    public boolean isAccepted() {
        if (packet != null) {
            return readBoolean(0);
        } else {
            return accepted;
        }
    }

    public void setAccepted(boolean isAccepted) {
        if (packet != null) {
            writeBoolean(0, isAccepted);
        } else {
            accepted = isAccepted;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        return packetConstructor.newInstance(getWindowId(), getActionNumber(), isAccepted());
    }
}
