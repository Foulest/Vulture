package io.github.retrooper.packetevents.packetwrappers.play.out.entitymetadata;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;

/**
 * @author SteelPhoenix, retrooper
 * @since 1.8
 */
class WrappedWatchableObject extends WrappedPacket {

    private static final int VALUE_INDEX = 2;

    WrappedWatchableObject(NMSPacket packet) {
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

    private Object getRawValue() {
        return readAnyObject(VALUE_INDEX);
    }

    public void setRawValue(Object rawValue) {
        writeAnyObject(VALUE_INDEX, rawValue);
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
