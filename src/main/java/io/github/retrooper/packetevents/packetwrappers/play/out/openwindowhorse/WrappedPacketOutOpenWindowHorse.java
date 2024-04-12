package io.github.retrooper.packetevents.packetwrappers.play.out.openwindowhorse;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Wrapper for the OpenWindowHorse packet.
 *
 * @author retrooper, Tecnio
 * @since 1.8
 */
public final class WrappedPacketOutOpenWindowHorse extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private int windowID;
    private int slotCount;

    public WrappedPacketOutOpenWindowHorse(NMSPacket packet) {
        super(packet, 2);
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.OPEN_WINDOW_HORSE.getConstructor(int.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
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

    public int getSlotCount() {
        if (packet != null) {
            return readInt(1);
        } else {
            return slotCount;
        }
    }

    public void setSlotCount(int slotCount) {
        if (packet != null) {
            writeInt(1, slotCount);
        } else {
            this.slotCount = slotCount;
        }
    }

    @Override
    public boolean isSupported() {
        return version.isNewerThan(ServerVersion.v_1_15_2);
    }

    @Override
    public @NotNull Object asNMSPacket() throws Exception {
        return packetConstructor.newInstance(getWindowId(), getSlotCount(), getEntityId());
    }
}
