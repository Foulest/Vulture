package io.github.retrooper.packetevents.packetwrappers.play.out.entitymetadata;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.google.OptionalUtils;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.server.ServerVersion;

/**
 * @author SteelPhoenix, retrooper
 * @since 1.8
 */
public class WrappedWatchableObject extends WrappedPacket {

    private static int valueIndex = 2;
    private static Class<?> googleOptionalClass;

    public WrappedWatchableObject(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        if (version.isNewerThan(ServerVersion.v_1_8_8)) {
            valueIndex = 1;

            try {
                googleOptionalClass = Class.forName("com.google.common.base.Optional");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public int getIndex() {
        if (version.isNewerThan(ServerVersion.v_1_8_8)) {
            Object dataWatcherObject = readAnyObject(0);
            WrappedPacket wrappedDataWatcher = new WrappedPacket(new NMSPacket(dataWatcherObject));
            return wrappedDataWatcher.readInt(0);
        } else {
            return readInt(0);
        }
    }

    public void setIndex(int index) {
        if (version.isNewerThan(ServerVersion.v_1_8_8)) {
            Object dataWatcherObject = readAnyObject(0);
            WrappedPacket wrappedDataWatcher = new WrappedPacket(new NMSPacket(dataWatcherObject));
            wrappedDataWatcher.writeInt(0, index);
        } else {
            writeInt(0, index);
        }
    }

    public boolean isDirty() {
        return readBoolean(0);
    }

    public void setDirty(boolean dirty) {
        writeBoolean(0, dirty);
    }

    public Object getRawValue() {
        return readAnyObject(valueIndex);
    }

    public void setRawValue(Object rawValue) {
        writeAnyObject(valueIndex, rawValue);
    }

    // TODO: Finish get WrappedWatchableObject#getValue
    protected Object getValue() {
        Object rawValue = getRawValue();
        Class<?> rawType = rawValue.getClass();

        if (rawType.equals(googleOptionalClass)) {
            return OptionalUtils.convertToJavaOptional(rawValue);
        } else if (rawType.equals(NMSUtils.iChatBaseComponentClass)) {
            // TODO: make wrapper for iChatBaseComponents
            return rawValue;
        } else if (rawType.equals(NMSUtils.nmsItemStackClass)) {
            return NMSUtils.toBukkitItemStack(rawValue);
        } else {
            // TODO: the rest of the classes
            return rawValue;
        }
    }

    // TODO: Finish WrappedWatchableObject#setValue
    protected void setValue(Object value) {

    }
}
