package io.github.retrooper.packetevents.packetwrappers.api;

public interface WrapperPacketWriter {

    void writeBoolean(int index, boolean value);

    void writeByte(int index, byte value);

    void writeShort(int index, short value);

    void writeInt(int index, int value);

    void writeLong(int index, long value);

    void writeFloat(int index, float value);

    void writeDouble(int index, double value);

    void writeString(int index, String value);

    // ARRAYS

    void writeBooleanArray(int index, boolean[] array);

    void writeByteArray(int index, byte[] value);

    void writeShortArray(int index, short[] value);

    void writeIntArray(int index, int[] value);

    void writeLongArray(int index, long[] value);

    void writeFloatArray(int index, float[] value);

    void writeDoubleArray(int index, double[] value);

    void writeStringArray(int index, String[] value);

    void writeObject(int index, Object object);

    void writeAnyObject(int index, Object value);

    void writeEnumConstant(int index, Enum<?> enumConstant);
}
