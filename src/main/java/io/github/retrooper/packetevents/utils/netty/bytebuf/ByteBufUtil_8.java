package io.github.retrooper.packetevents.utils.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.EmptyArrays;

public final class ByteBufUtil_8 implements ByteBufUtil {

    @Override
    public Object newByteBuf(byte[] data) {
        return Unpooled.wrappedBuffer(data);
    }

    @Override
    public void retain(Object byteBuf) {
        ((ByteBuf) byteBuf).retain();
    }

    @Override
    public void release(Object byteBuf) {
        ((ByteBuf) byteBuf).release();
    }

    @Override
    public byte[] getBytes(Object byteBuf) {
        ByteBuf bb = (ByteBuf) byteBuf;

        if (bb.refCnt() < 1) {
            return EmptyArrays.EMPTY_BYTES;
        }

        byte[] bytes;

        if (bb.hasArray()) {
            bytes = bb.array();
        } else {
            bytes = new byte[bb.readableBytes()];
            bb.getBytes(bb.readerIndex(), bytes);
        }
        return bytes;
    }

    @Override
    public void setBytes(Object byteBuf, byte[] bytes) {
        ByteBuf bb = (ByteBuf) byteBuf;

        if (bb.refCnt() < 1) {
            return;
        }

        int bytesLength = bytes.length;

        if (bb.capacity() < bytesLength) {
            bb.capacity(bytesLength);
        }

        bb.setBytes(0, bytes);
    }
}
