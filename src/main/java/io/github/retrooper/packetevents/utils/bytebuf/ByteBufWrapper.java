package io.github.retrooper.packetevents.utils.bytebuf;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ByteBufWrapper {

    private final ByteBuf byteBuf;

    public ByteBuf get() {
        return byteBuf;
    }

    public int readInt() {
        return byteBuf.readInt();
    }

    public boolean readBoolean() {
        return byteBuf.readBoolean();
    }

    public byte readByte() {
        return byteBuf.readByte();
    }

    public char readChar() {
        return byteBuf.readChar();
    }

    public double readDouble() {
        return byteBuf.readDouble();
    }

    public float readFloat() {
        return byteBuf.readFloat();
    }

    public long readLong() {
        return byteBuf.readLong();
    }

    public short readShort() {
        return byteBuf.readShort();
    }

    public String readString() {
        String output = null;

        for (int i = 0; i < byteBuf.capacity(); i++) {
            if (output == null) {
                output = "";
            }

            byte b = byteBuf.getByte(i);
            output = output.concat(String.valueOf((char) b));
        }
        return output;
    }

    public boolean isReadable() {
        return byteBuf.isReadable();
    }
}
