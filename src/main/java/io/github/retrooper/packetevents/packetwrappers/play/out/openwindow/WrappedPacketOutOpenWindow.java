package io.github.retrooper.packetevents.packetwrappers.play.out.openwindow;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import lombok.ToString;

import java.util.Optional;

@ToString
public class WrappedPacketOutOpenWindow extends WrappedPacket {

    private static boolean legacyMode;
    private static boolean ultraLegacyMode;
    private int windowID;
    private int windowTypeID;
    @Deprecated
    private String windowType;
    private String windowTitle;

    public WrappedPacketOutOpenWindow(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        // Older versions (like 1.13.2 and lower) contain a String,
        legacyMode = Reflection.getField(PacketTypeClasses.Play.Server.OPEN_WINDOW, String.class, 0) != null;
        // 1.7.10
        ultraLegacyMode = Reflection.getField(PacketTypeClasses.Play.Server.OPEN_WINDOW, boolean.class, 0) != null;
    }

    public int getWindowId() {
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

    public Optional<Integer> getInventoryTypeId() {
        if (nmsPacket != null) {
            if (legacyMode && !ultraLegacyMode) {
                return Optional.empty();
            }
            return Optional.of(readInt(1));
        } else {
            return Optional.of(windowTypeID);
        }
    }

    public void setInventoryTypeId(int inventoryTypeID) {
        if (nmsPacket != null) {
            if (legacyMode && !ultraLegacyMode) {
                return;
            }

            writeInt(1, inventoryTypeID);
        } else {
            windowTypeID = inventoryTypeID;
        }
    }

    public Optional<String> getInventoryType() {
        if (nmsPacket != null) {
            if (!legacyMode || ultraLegacyMode) {
                return Optional.empty();
            }
            return Optional.of(readString(0));
        } else {
            return Optional.of(windowType);
        }
    }

    public String getWindowTitle() {
        if (nmsPacket != null) {
            if (ultraLegacyMode) {
                return readString(0);
            }
            return readIChatBaseComponent(0);
        }
        return windowTitle;
    }

    public void setWindowTitle(String title) {
        if (nmsPacket != null) {
            if (ultraLegacyMode) {
                writeString(0, title);
            } else {
                writeIChatBaseComponent(title);
            }
        }
    }
}
