package io.github.retrooper.packetevents.packetwrappers.play.out.entitymetadata;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;

/**
 * @author SteelPhoenix, retrooper
 * @since 1.8
 */
public class WrappedWatchableObject extends WrappedPacket {

    private static final int valueIndex = 2;

    public WrappedWatchableObject(NMSPacket packet) {
        super(packet);
    }

    public int getIndex() {
        return readInt(0);
    }

    public void setIndex(int index) {
        writeInt(0, index);
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

        if (rawType.equals(NMSUtils.iChatBaseComponentClass)) {
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
