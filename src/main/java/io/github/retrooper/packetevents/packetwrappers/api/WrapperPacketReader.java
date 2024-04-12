package io.github.retrooper.packetevents.packetwrappers.api;

public interface WrapperPacketReader {

    boolean readBoolean(int index);

    byte readByte(int index);

    short readShort(int index);

    int readInt(int index);

    long readLong(int index);

    float readFloat(int index);

    double readDouble(int index);

    boolean[] readBooleanArray(int index);

    byte[] readByteArray(int index);

    short[] readShortArray(int index);

    int[] readIntArray(int index);

    long[] readLongArray(int index);

    float[] readFloatArray(int index);

    double[] readDoubleArray(int index);

    String[] readStringArray(int index);

    String readString(int index);

    <T> T readObject(int index, Class<? extends T> type);

    Enum<?> readEnumConstant(int index, Class<? extends Enum<?>> type);

    Object readAnyObject(int index);
}
