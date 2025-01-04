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
package net.foulest.packetevents.utils.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCounted;
import io.netty.util.internal.EmptyArrays;

public final class ByteBufUtil_8 implements ByteBufUtil {

    @Override
    public Object newByteBuf(byte[] data) {
        return Unpooled.wrappedBuffer(data);
    }

    @Override
    public void retain(Object byteBuf) {
        ((ReferenceCounted) byteBuf).retain();
    }

    @Override
    public void release(Object byteBuf) {
        ((ReferenceCounted) byteBuf).release();
    }

    @Override
    public byte[] getBytes(Object byteBuf) {
        ByteBuf buf = (ByteBuf) byteBuf;

        if (buf.refCnt() < 1) {
            return EmptyArrays.EMPTY_BYTES;
        }

        byte[] bytes;

        if (buf.hasArray()) {
            bytes = buf.array();
        } else {
            bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
        }
        return bytes;
    }

    @Override
    public void setBytes(Object byteBuf, byte[] bytes) {
        ByteBuf buf = (ByteBuf) byteBuf;

        if (buf.refCnt() < 1) {
            return;
        }

        int bytesLength = bytes.length;

        if (buf.capacity() < bytesLength) {
            buf.capacity(bytesLength);
        }

        buf.setBytes(0, bytes);
    }
}
