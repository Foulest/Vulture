/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
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
