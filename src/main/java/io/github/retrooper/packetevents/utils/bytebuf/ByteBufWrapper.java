package io.github.retrooper.packetevents.utils.bytebuf;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ByteBufWrapper {

    private final ByteBuf byteBuf;

    public ByteBuf get() {
        return this.byteBuf;
    }

    public int readInt() {
        return this.byteBuf.readInt();
    }

    public boolean readBoolean() {
        return this.byteBuf.readBoolean();
    }

    public byte readByte() {
        return this.byteBuf.readByte();
    }

    public char readChar() {
        return this.byteBuf.readChar();
    }

    public double readDouble() {
        return this.byteBuf.readDouble();
    }

    public float readFloat() {
        return this.byteBuf.readFloat();
    }

    public long readLong() {
        return this.byteBuf.readLong();
    }

    public short readShort() {
        return this.byteBuf.readShort();
    }

    public String readString() {
        String output = null;

        for (int i = 0; i < this.byteBuf.capacity(); i++) {
            if (output == null) {
                output = "";
            }

            byte b = this.byteBuf.getByte(i);
            output = output.concat(String.valueOf((char) b));
        }
        return output;
    }

    public boolean isReadable() {
        return this.byteBuf.isReadable();
    }
}
