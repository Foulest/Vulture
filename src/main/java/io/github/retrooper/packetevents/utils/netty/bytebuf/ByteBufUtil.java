package io.github.retrooper.packetevents.utils.netty.bytebuf;

public interface ByteBufUtil {

    Object newByteBuf(byte[] data);

    void retain(Object byteBuf);

    void release(Object byteBuf);

    byte[] getBytes(Object byteBuf);

    void setBytes(Object byteBuf, byte[] bytes);
}
